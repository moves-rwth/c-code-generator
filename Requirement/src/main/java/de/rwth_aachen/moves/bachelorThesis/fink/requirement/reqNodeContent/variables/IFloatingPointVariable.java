package de.rwth_aachen.moves.bachelorThesis.fink.requirement.reqNodeContent.variables;

import java.math.BigDecimal;

public interface IFloatingPointVariable {
	BigDecimal getValue();

	void setValue(BigDecimal value);

	BigDecimal getMin();

	void setMin(BigDecimal min);

	BigDecimal getMax();

	void setMax(BigDecimal max);
}
