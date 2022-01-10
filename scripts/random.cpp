#include <random>

static std::random_device random_dev;
static std::mt19937 generator(random_dev());

extern "C" signed char __VERIFIER_nondet_char(signed char x, signed char y) {
	std::uniform_int_distribution<signed char> distrib(x, y);
	return distrib(generator);
}

extern "C" unsigned char __VERIFIER_nondet_uchar(unsigned char x, unsigned char y) {
	std::uniform_int_distribution<unsigned char> distrib(x, y);
	return distrib(generator);
}

extern "C" signed short __VERIFIER_nondet_short(signed short x, signed short y) {
	std::uniform_int_distribution<signed short> distrib(x, y);
	return distrib(generator);
}

extern "C" unsigned short __VERIFIER_nondet_ushort(unsigned short x, unsigned short y) {
	std::uniform_int_distribution<unsigned short> distrib(x, y);
	return distrib(generator);
}

extern "C" signed long int __VERIFIER_nondet_long(signed long int x, signed long int y) {
	std::uniform_int_distribution<signed long> distrib(x, y);
	return distrib(generator);
}

extern "C" unsigned long int __VERIFIER_nondet_ulong(unsigned long int x, unsigned long int y) {
	std::uniform_int_distribution<unsigned long> distrib(x, y);
	return distrib(generator);
}

extern "C" float __VERIFIER_nondet_float(float x, float y) {
	std::uniform_real_distribution<float> distrib(x, y);
	return distrib(generator);
}

extern "C" double __VERIFIER_nondet_double(double x, double y) {
	std::uniform_real_distribution<double> distrib(x, y);
	return distrib(generator);
}
