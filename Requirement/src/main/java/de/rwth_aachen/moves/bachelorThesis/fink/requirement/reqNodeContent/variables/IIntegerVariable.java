package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

public interface IIntegerVariable {

	Long getValue();

	void setValue(Long value);

	Long getMin();

	void setMin(Long min);

	Long getMax();

	void setMax(Long max);
}
