package de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.beforesortingpostprocessors;

import de.rwth_aachen.moves.bachelorThesis.fink.codeGenerator.reqcreation.postProcessing.IPostProcessor;

/**
 * Post-processors which can only be applied to generated requirements before sorting them based on their dependencies.
 * If a post-processor implements this interface they modify requirements in a way which can also change the variable dependency graph.
 */
public interface IBeforeSortingPostProcessor extends IPostProcessor {
}
