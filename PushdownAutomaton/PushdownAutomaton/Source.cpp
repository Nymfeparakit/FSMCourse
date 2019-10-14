#include "PushDownAutomaton.hpp"


int main()
{
	setlocale(LC_ALL, "Russian");
	PushDownAutomaton automaton("test4.txt");
	automaton.print();
	std::cout << std::endl;
	for (;;) {
		std::cout << "Line: ";
		std::string line;
		std::getline(std::cin, line);
		if (!automaton.parseInput(line)) {
			std::cout << "Строка не является допустимой" << std::endl;
		}
		std::cout << std::endl;
	}
	std::getchar();
}