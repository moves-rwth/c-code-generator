package de.rwth_aachen.moves.bachelorThesis.fink.codeParser.treeHandling.tree;

public class SectionMarker implements ITree {
	private final String sectionName;
	private final ITree child;

	public SectionMarker(String sectionName, ITree child) {
		this.sectionName = sectionName;
		this.child = child;
	}

	public static SectionMarker createSectionMarker(String sectionName, ITree child) {
		return new SectionMarker(sectionName, child);
	}

	public String getSectionName() {
		return sectionName;
	}

	public ITree getChild() {
		return child;
	}

}
