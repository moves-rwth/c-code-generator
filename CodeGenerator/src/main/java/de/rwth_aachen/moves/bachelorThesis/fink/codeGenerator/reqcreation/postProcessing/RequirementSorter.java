package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.cmd.CodePropertiesConfig;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.Requirement;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.handling.IProgramContext;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.IVariableWithAccessor;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables.IVariable;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.codetreeobjects.Property;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.IVariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqTree.util.VariableCollector;
import de.rwth_aachen.moves.bachelorThesis.fink.requirement.templating.TemplateInfo;
import org.javatuples.Pair;
import org.javatuples.Triplet;

import java.util.*;

/**
 * PostProcessor used for creating a sorting of all requirements such that all dependencies occur in a non-problematic order.
 * If circular dependencies exist, then this class also breaks these.
 * Variables may not be read after being written once, so the property is correct.
 */
public class RequirementSorter {

	public Triplet<TemplateInfo, TemplateInfo, Map<String, Integer>> apply(TemplateInfo fromTemplate, TemplateInfo fromRandomGen, CodePropertiesConfig config) {
		// Collecting all requirements and properties
		List<Requirement> allRequirements = new ArrayList<>(fromTemplate.getRequirements());
		allRequirements.addAll(fromRandomGen.getRequirements());
		List<Property> allProperties = new ArrayList<>(fromTemplate.getProperties());
		allProperties.addAll(fromRandomGen.getProperties());

		// Actual loop resolving of the requirement dependencies
		Pair<List<Requirement>, List<Property>> result = sortRequirements(allRequirements, allProperties, fromTemplate.getProgramContext());

		// Helper list for later
		Map<String, Integer> sorting = new HashMap<>();
		for (int i = 0; i < result.getValue0().size(); i++) {
			sorting.put(result.getValue0().get(i).getName(), i);
		}

		// Update TemplateInfo to the new requirements
		for (Requirement req : result.getValue0()) {
			ListIterator<Requirement> reqIterator = fromRandomGen.getRequirements().listIterator();
			while (reqIterator.hasNext()) {
				Requirement current = reqIterator.next();
				if (current.getName().equals(req.getName())) {
					reqIterator.set(req);
				}
			}
			ListIterator<Requirement> reqIterator2 = fromTemplate.getRequirements().listIterator();
			while (reqIterator2.hasNext()) {
				Requirement current = reqIterator2.next();
				if (current.getName().equals(req.getName())) {
					reqIterator2.set(req);
				}
			}
		}

		// Update TemplateInfo to the changed properties
		for (Property prop : result.getValue1()) {
			ListIterator<Property> propIterator = fromRandomGen.getProperties().listIterator();
			while (propIterator.hasNext()) {
				Property current = propIterator.next();
				if (current.getName().equals(prop.getName())) {
					propIterator.set(prop);
				}
			}
			ListIterator<Property> propIterator2 = fromTemplate.getProperties().listIterator();
			while (propIterator2.hasNext()) {
				Property current = propIterator2.next();
				if (current.getName().equals(prop.getName())) {
					propIterator2.set(prop);
				}
			}
		}

		return new Triplet<>(fromTemplate, fromRandomGen, sorting);
	}

	public Pair<List<Requirement>, List<Property>> sortRequirements(List<Requirement> requirements, List<Property> properties, IProgramContext context) {
		// Map an Integer (identifier) to each requirement and collect all outputVars
		Map<Integer, Requirement> requirementMap = new HashMap<>();
		List<IVariableWithAccessor> allOutputVars = new ArrayList<>();
		for (int i = 0; i < requirements.size(); i++) {
			requirementMap.put(i, requirements.get(i));
			IVariableCollector variableCollector = new VariableCollector(true, context);
			requirements.get(i).accept(variableCollector);
			allOutputVars.addAll(variableCollector.getOutputVariablesWithAccessor());
		}

		List<Integer> unsortedList = new ArrayList<>(requirementMap.keySet());
		List<Integer> sortedList = new ArrayList<>();
		// Maps to the requirements which have to occur after the requirement
		Map<Integer, Set<Integer>> requirementToSuccessors = createDependencyMap(requirementMap, context);

		// Maps to the requirements which have to occur before the requirement
		Map<Integer, Set<Integer>> requirementToPredecessors;

		boolean weHaveLoops;
		do {
			// Do looped dependencies exist?
			weHaveLoops = false;
			Map<Integer, Set<Integer>> requirementToLoopedSuccessors = findLoopedDependencies(requirementMap, requirementToSuccessors);
			for (Integer req: unsortedList) {
				if (!requirementToLoopedSuccessors.get(req).isEmpty()) {
					weHaveLoops = true;
					break;
				}
			}

			// Maps to the requirements which have to occur before the requirement
			requirementToPredecessors = new HashMap<>();
			for (Integer entry: requirementMap.keySet()) {
				Set<Integer> dependencies = new HashSet<>();
				for (Integer subEntry: requirementMap.keySet()) {
					if (requirementToSuccessors.get(subEntry).contains(entry)) dependencies.add(subEntry);
				}
				requirementToPredecessors.put(entry, dependencies);
			}

			if (weHaveLoops) resolveLoops(allOutputVars, unsortedList, requirementToPredecessors, requirementToSuccessors, requirementToLoopedSuccessors, requirementMap, context, properties);
		} while (weHaveLoops);

		// Sort the list based on the dependencies (assuming we have no loops!)
		while (!unsortedList.isEmpty()) {
			Iterator<Integer> iterator = unsortedList.iterator();
			while (iterator.hasNext()) {
				Integer req = iterator.next();
				if (requirementToPredecessors.get(req).isEmpty()) {
					sortedList.add(req);
					iterator.remove();
					requirementToPredecessors.forEach((requirement, dependencyList) -> dependencyList.remove(req));
				}
			}
		}

		List<Requirement> outputReq = new ArrayList<>();
		for (Integer req : sortedList) {
			outputReq.add(requirementMap.get(req));
		}
		return new Pair<>(outputReq, properties);
	}

	private void resolveLoops(List<IVariableWithAccessor> allOutputVars, List<Integer> unsortedList, Map<Integer, Set<Integer>> requirementToPredecessors, Map<Integer, Set<Integer>> requirementToSuccessors, Map<Integer, Set<Integer>> requirementToLoopedSuccessors, Map<Integer, Requirement> requirementMap, IProgramContext context, List<Property> properties) {
		// Sorting by the largest set of looped successors first (the req with most dependencies)
		unsortedList.sort(Comparator.comparing(req -> requirementToLoopedSuccessors.get((Integer) req).size()).reversed());

		// Choose the first requirement as it causes the most problems
		final Integer chosenReqId = unsortedList.get(0);
		final Requirement chosenReq = requirementMap.get(chosenReqId);
		IVariableCollector variableCollectorChosenReq = new VariableCollector(false, context);
		chosenReq.accept(variableCollectorChosenReq);

		// Find all vars that have to be fixed
		Set<IVariableWithAccessor> toFixVars = new HashSet<>();
		for (Integer problem : requirementToLoopedSuccessors.get(chosenReqId)) {
			for (IVariableWithAccessor nonOutputVar : variableCollectorChosenReq.getInputVariablesWithAccessorWithoutOutput()) {
				if (!allOutputVars.contains(nonOutputVar)) continue; // Check if this variable is written at all within any requirement
				boolean hasToBeFixed = false;
				for (Requirement req : requirementMap.values()) {
					if (req != chosenReq) {
						IVariableCollector collector = new VariableCollector(false, context);
						req.accept(collector);
						if (collector.getUsedVariablesWithAccessor().contains(nonOutputVar)) {
							hasToBeFixed = true;
							break;
						}
					}
				}
				if (hasToBeFixed) toFixVars.add(nonOutputVar);

				requirementToPredecessors.get(chosenReqId).remove(problem);
				requirementToSuccessors.get(problem).remove(chosenReqId);
			}
		}

		// Replace variables with last(var) within the chosen requirement
		requirementMap.put(chosenReqId, chosenReq.addLastBeforeVariables(toFixVars));

		// Change the properties according to the change within the requirement
		ListIterator<Property> propertyIterator = properties.listIterator();
		while (propertyIterator.hasNext()) {
			Property current = propertyIterator.next();
			if (current.getCorrespondingRequirementNames().contains(chosenReq.getName())) {
				propertyIterator.set(current.addLastBeforeVariables(toFixVars));
			}
		}

		// Remove dependencies on the chosen requirement due to the changes
		requirementToPredecessors.get(chosenReqId).clear();
		for (Integer req : requirementToSuccessors.keySet()) {
			requirementToSuccessors.get(req).remove(chosenReqId);
		}
	}

	private Map<Integer, Set<Integer>> createDependencyMap(Map<Integer, Requirement> requirementMap, IProgramContext context) {
		Map<Integer, Set<Integer>> requirementToSuccessors = new HashMap<>();
		for (Map.Entry<Integer, Requirement> entry : requirementMap.entrySet()) {
			Set<Integer> dependents = new HashSet<>();
			final Requirement req = entry.getValue();
			IVariableCollector variableCollector = new VariableCollector(false, context);
			req.accept(variableCollector);
			Set<IVariable> outputVars = variableCollector.getOutputVariables();

			for (Map.Entry<Integer, Requirement> subEntry : requirementMap.entrySet()) {
				final Requirement otherReq = subEntry.getValue();
				if (req == otherReq) continue;

				IVariableCollector variableCollectorOtherRequirement = new VariableCollector(false, context);
				otherReq.accept(variableCollectorOtherRequirement);

				Set<IVariable> otherNonOutputVars = variableCollectorOtherRequirement.getInputVariablesWithoutOutputVariables();
				Set<IVariable> otherOutputVars = variableCollectorOtherRequirement.getOutputVariables();
				if (!Collections.disjoint(outputVars, otherNonOutputVars)) {
					dependents.add(subEntry.getKey());
				}
			}
			requirementToSuccessors.put(entry.getKey(), dependents);
		}

		return requirementToSuccessors;
	}

	private Map<Integer, Set<Integer>> findLoopedDependencies(Map<Integer, Requirement> requirements, Map<Integer, Set<Integer>> requirementToSuccessors) {
		Map<Integer, Set<Integer>> requirementToLoopedSuccessors = new HashMap<>();

		// Init direct dependents
		Map<Integer, Set<Integer>> requirementToSuccessorsRecursive = new HashMap<>();
		for (Integer entry: requirements.keySet()) {
			requirementToSuccessorsRecursive.put(entry, new HashSet<>(requirementToSuccessors.get(entry)));
		}

		// Get recursive dependents
		for (Integer entry: requirements.keySet()) {
			Set<Integer> dependents = new HashSet<>(requirementToSuccessorsRecursive.get(entry));
			Set<Integer> alreadyChecked = new HashSet<>();
			alreadyChecked.add(entry);
			for (Integer toCheck: dependents) {
				checkRequirement(alreadyChecked, toCheck, entry, requirementToSuccessorsRecursive);
			}
		}

		// Add all looped dependencies (requirements dependent on each other) to the result
		for (Integer entry: requirements.keySet()) {
			Set<Integer> loopedSuccessors = new HashSet<>();
			for (Integer otherReq : requirementToSuccessors.get(entry)) {
				if (requirementToSuccessorsRecursive.get(otherReq).contains(entry)) {
					loopedSuccessors.add(otherReq);
				}
			}
			requirementToLoopedSuccessors.put(entry, loopedSuccessors);
		}

		return requirementToLoopedSuccessors;
	}

	private void checkRequirement(Set<Integer> alreadyChecked, Integer req, Integer rootRequirement, Map<Integer, Set<Integer>> requirementToSuccessors) {
		if (!alreadyChecked.contains(req)) {
			requirementToSuccessors.get(rootRequirement).add(req);
			alreadyChecked.add(req);
			for (Integer toCheck : requirementToSuccessors.get(req)) {
				checkRequirement(alreadyChecked, toCheck, rootRequirement, requirementToSuccessors);
			}
		}
	}
}
