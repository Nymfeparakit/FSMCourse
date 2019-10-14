#include "PushDownAutomaton.hpp"


int main()
{
	PushDownAutomaton automaton("test4.txt");
	automaton.print();
	automaton.parseInput("a+a*a");
	std::getchar();
}