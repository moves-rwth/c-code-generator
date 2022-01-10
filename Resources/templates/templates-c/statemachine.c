// CONTROL:SIGNAL_INPUT
unsigned char inputA = range(0, 1);
unsigned char inputB = range(0, 1);
unsigned char inputC = range(0, 1);
unsigned char inputD = range(0, 1);

// CONTROL:SIGNAL_OUTPUT
unsigned char outputSignal;

// CONTROL:INTERNAL_VARS
unsigned char StateA = 3;
unsigned char IsActive = 0;
unsigned char State = 0;
unsigned char TimedState = 0;
float Timer = 0.0f;
float TimerStartValue = 0.0f;

// CONTROL:CALIBRATABLES
unsigned char STATE_1_SIGNAL = 1;
unsigned char STATE_2_SIGNAL = 2;
unsigned char STATE_3_SIGNAL = 3;
unsigned char STATE_1 = 1;
unsigned char STATE_2 = 2;
unsigned char STATE_3 = 3;
unsigned char inputC_ZERO = 0;
float TIMER_INC = 0.01f;
float TIMER_MAX = 3600.0f;
float DELAY = 0.05f;

// CONTROL:FUNCTIONS
boolean helper_func1() {
	return (StateA == STATE_1) && (inputB == 0);
}
boolean helper_func2() {
	return (!helper_func5()) && (inputA == 0) && (inputC == inputC_ZERO);
}
boolean helper_func3() {
	return (StateA == STATE_3) && (inputD != 0);
}
boolean helper_func4() {
	return (!helper_func3()) && (StateA == STATE_3) && (inputA != 0);
}
boolean helper_func5() {
	return (StateA == STATE_2) && (inputB != 0);
}
boolean timer_func(float t0, float t1, float duration) {
	boolean res;
	float diff;
	if (t0 <= 0.0F) {
		res = 1;
	} else {
		if (t1 < t0) {
			diff = (TIMER_MAX - t0) + t1;
		} else {
			diff = t1 - t0;
		}
		res = (diff >= duration);
	}
	return (res);
}

// CONTROL:CODE
void code() {
	Timer = TIMER_INC + Timer;
    if (!(Timer < TIMER_MAX)) {
		Timer = TIMER_INC;
    }
	
	if (IsActive == 0U) {
		IsActive = 1U;
		State = (2U);

		outputSignal = STATE_1_SIGNAL;
    } else {
		switch (State) {
			case (1U):
				if ((StateA == STATE_3) && inputD) {
					State = (2U);

					outputSignal = STATE_1_SIGNAL;
				} else {
					if ((StateA == STATE_3) && inputA) {
						State = 3U;
						outputSignal = STATE_2_SIGNAL;
						TimedState = (1U);
				  }
				}
				break;

			case (2U):
				if ((StateA == STATE_1) && (!inputB)) {
					State = (3U);
					outputSignal = STATE_2_SIGNAL;

					if (inputA || (inputC != inputC_ZERO)) {
						TimedState = (1U);
					} else {
						TimedState = (2U);
						TimerStartValue = Timer;
					}
				}
				break;
			
			default:
				if ((StateA == STATE_2) && inputB) {
					TimedState = (0U);
					State =	(2U);
					outputSignal = STATE_1_SIGNAL;
				} else if (TimedState == (1U)) {
					if ((!inputA) && (inputC == inputC_ZERO)) {
						TimedState = (2U);
						TimerStartValue = Timer;
					}
				} else {
					if (inputA || (inputC != inputC_ZERO)) {
						TimedState = (1U);
					} else {
						if ((StateA == STATE_2) && timer_func(TimerStartValue, Timer, DELAY)) {
							TimedState = (0U);
							State = (1U);
							outputSignal = STATE_3_SIGNAL;
						}
					}
				}
				break;
		}
    }
}

// CONTROL:LOCAL_PROPERTIES
int local_property1 = (last(outputSignal) != STATE_1_SIGNAL) || (
	(
		( ! helper_func1()) || (out(outputSignal) == STATE_2_SIGNAL)
	) && (
		((helper_func1())) || (outputSignal == STATE_1_SIGNAL)
	)
);
int local_property2 = (last(outputSignal) != STATE_3_SIGNAL) || (
	(
		( ! helper_func3()) || (out(outputSignal) == STATE_1_SIGNAL)
	) && (
		(helper_func3()) || (
			( ! helper_func4()) || (outputSignal == STATE_2_SIGNAL) &&
			(
				(helper_func4()) || (outputSignal == STATE_3_SIGNAL)
			)
		)
	)
);
int local_property3 = (last(outputSignal) != STATE_2_SIGNAL) || (
	(
		( ! helper_func5()) || (out(outputSignal) == STATE_1_SIGNAL)
	) && (
		(helper_func5()) ||
		(
			(outputSignal == STATE_2_SIGNAL) | (outputSignal == STATE_3_SIGNAL)
		)
	)
);
int local_property4 = ! (
        (StateA == STATE_2) && (
                (outputSignal == STATE_2_SIGNAL) && (inputA == 0) && (inputB == 0) &&
                (last(outputSignal) == STATE_2_SIGNAL) && (last(inputA) == 0) && (last(inputB) == 0) &&
                (last_i(outputSignal, 2) == STATE_2_SIGNAL) && (last_i(inputA, 2) == 0) && (last_i(inputB, 2) == 0) &&
                (last_i(outputSignal, 3) == STATE_2_SIGNAL) && (last_i(inputA, 3) == 0) && (last_i(inputB, 3) == 0) &&
                (last_i(outputSignal, 4) == STATE_2_SIGNAL) && (last_i(inputA, 4) == 0) && (last_i(inputB, 4) == 0)
        )
) || (
        out(outputSignal) == STATE_3_SIGNAL
);
int local_property5 = ! ((out(outputSignal) == STATE_3_SIGNAL) && (last(outputSignal) == STATE_2_SIGNAL)) || (
        (StateA == STATE_2) && (
                (outputSignal == STATE_2_SIGNAL) && (inputA == 0) && (inputB == 0) &&
                (last(outputSignal) == STATE_2_SIGNAL) && (last(inputA) == 0) && (last(inputB) == 0) &&
                (last_i(outputSignal, 2) == STATE_2_SIGNAL) && (last_i(inputA, 2) == 0) && (last_i(inputB, 2) == 0) &&
                (last_i(outputSignal, 3) == STATE_2_SIGNAL) && (last_i(inputA, 3) == 0) && (last_i(inputB, 3) == 0) &&
                (last_i(outputSignal, 4) == STATE_2_SIGNAL) && (last_i(inputA, 4) == 0) && (last_i(inputB, 4) == 0)
        )
);

// CONTROL:GLOBAL_PROPERTIES
int global_property1 = (0 <= State) && (State <= 3);
