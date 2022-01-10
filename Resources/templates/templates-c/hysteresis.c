// CONTROL:SIGNAL_INPUT
float input_hysteresis = range(0f, 100f);

// CONTROL:SIGNAL_OUTPUT
unsigned char state_hysteresis;

// CONTROL:LOCALS
float lowerLimit;
float upperLimit;

// CONTROL:CALIBRATABLES
float hysteresis_thres = 10.0f;
float hysteresis_thres_mod = 3.0f;

// CONTROL:CODE
void code() {
	lowerLimit = hysteresis_thres - hysteresis_thres_mod;
	upperLimit = hysteresis_thres + hysteresis_thres_mod;
	state_hysteresis = ((input_hysteresis > upperLimit ) || ((!(input_hysteresis <= lowerLimit )) && last(state_hysteresis)));
}

// CONTROL:LOCAL_PROPERTIES
int local_property1 = out(state_hysteresis) == ((input_hysteresis > (hysteresis_thres + hysteresis_thres_mod)) || ((input_hysteresis <= (hysteresis_thres - hysteresis_thres_mod)) && (last(state_hysteresis) == 1)));

// CONTROL:GLOBAL_PROPERTIES
int global_property1 = ((state_hysteresis == 1) || (state_hysteresis == 0)) ? 1 : 0;