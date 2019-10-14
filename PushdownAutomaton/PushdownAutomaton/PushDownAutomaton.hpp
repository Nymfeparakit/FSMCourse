#pragma once
#include <fstream>
#include <string>
#include <set>
#include <map>
#include <utility>
#include <iostream>
#include <algorithm>
#include <stack>
#include <vector>

//TODO define isNonTerminal isupper

class PushDownAutomaton
{
public:
	PushDownAutomaton(std::string);
	void print();
	bool parseInput(std::string);
	bool process(std::stack<char>, 
		std::vector<std::string>&, 
		std::vector<std::string>&,
		std::string);
	bool processRulesForNonterminalSmbl(char, 
		std::vector<std::string>, 
		std::vector<std::string>,
		std::stack<char>);
private:
	std::set<char> p;//только терминальные символы
	std::set<char> z;
	char startSymbol;
	//{{входной символ, верхний символ стека}, список переходов}
	std::map<std::pair<std::string, char>, std::set<std::string>> commands;
	void createSecondTypeCommands();
	void insertSymbolToSets(std::string);
	void printSet(std::set<char>);
	void printCommands();
	void printConfigSequence(std::stack<std::string>, std::stack<std::string>);
};

PushDownAutomaton::PushDownAutomaton(std::string fileName)
{
	std::ifstream infile(fileName);
	std::string line;
	bool firstLine = true;
	while (std::getline(infile, line)) {

		//TODO проверяем строку на соответствие шаблону

		char nonTermSymbol = line[0];
		if (firstLine) {//если это первая строка
			startSymbol = nonTermSymbol;
			firstLine = false;
		}
		z.insert(nonTermSymbol);//слева всегда нетерминальный символ, так как это кс грамматика
		std::string rightSide = line.substr(2, line.length() - 2);//берем правую часть
		std::string rule;
		std::set<std::string> currRules;//правила для текущего символа
		size_t posOfDelimeter = rightSide.find('|');
		if (posOfDelimeter == std::string::npos) {//если в строке только одно правило
			std::reverse(rightSide.begin(), rightSide.end());//отражаем зеркально
			currRules.insert(rightSide);//добавляем всю правую часть
			insertSymbolToSets(rightSide);
		}
		else do {
			rule = rightSide.substr(0, posOfDelimeter);
			std::reverse(rule.begin(), rule.end());//отражаем зеркально
			insertSymbolToSets(rule);
			currRules.insert(rule);//добавляем его в список
			rightSide.erase(0, posOfDelimeter + 1);
			//} while ((posOfDelimeter = rightSide.find('|')) != std::string::npos);
		} while (rightSide.length() != 0);
		//составляем команду типа 1
		std::pair<std::string, char> p = std::make_pair("lambda", nonTermSymbol);
		commands.insert(std::make_pair(p, currRules));

	}

	createSecondTypeCommands();//составляем команды второго типа
	//последняя команда
	std::set<std::string> rule = { "lambda" };
	//оставим NULL для указания на дно стека
	std::pair<std::string, char> p = std::make_pair("lambda", NULL);
	commands.insert(std::make_pair(p, rule));

}

void PushDownAutomaton::insertSymbolToSets(std::string str) 
{
	for (int i = 0; i < str.length(); ++i) { //проходимся по строке, чтобы 
												   //дополнить множества
		char ch = str[i];
		z.insert(ch);
		if (!isupper(ch)) p.insert(ch);
	}
}

void PushDownAutomaton::createSecondTypeCommands() {

	//находим все терминальные символы
	std::set<char>::iterator it;
	//for (it = z.begin(); it != z.end(); ++it) {
	for (char ch : z) {
		//char ch = *it;
		if (isupper(ch)) { //нетерминальные - только заглавные 
			continue;
		}
		//составляем команду
		std::pair<std::string, char> p = std::make_pair(std::string(1, ch), ch);
		std::set<std::string> currRules = { "lambda" };
		commands.insert(std::make_pair(p, currRules));
	}

}

void PushDownAutomaton::print() 
{
	//печатаем множества Z, P
	std::cout << "P = {";
	printSet(p);
	std::cout << "}" << std::endl;
	std::cout << "Z = {";
	printSet(z);
	std::cout << ", h0}" << std::endl;

	printCommands();//печатаем команды
}

void PushDownAutomaton::printSet(std::set<char> s)
{
	std::set<char>::iterator it;
	it = s.begin();
	std::cout << *it;//печатаем первый символ
	for (++it; it != s.end(); ++it) {
		std::cout << ", " << *it;
	}
}

void PushDownAutomaton::printCommands()
{
	std::map<std::pair<std::string, char>, std::set<std::string>>::iterator it;
	//сначала проходим все нетерминальные символы
	for (it = commands.begin(); it != commands.end(); ++it) {
		std::pair<std::string, char> p = it->first;
		char ch = p.second;
		if (!isupper(ch)) continue;
		std::cout << "delta(s0, ";
		std::cout << p.first << ", " << p.second << ") = ";

		std::set<std::string> rules = it->second;
		std::cout << "{";
		std::set<std::string>::iterator set_it;
		set_it = rules.begin();
		std::cout << "(s0, " << *set_it << ")";//печатаем первое правило
		for (++set_it; set_it != rules.end(); ++set_it) {
			std::cout << "; (s0, " << *set_it << ")";
		}
		std::cout << "}" << std::endl;
	}


	//теперь все терминальные
	for (it = commands.begin(); it != commands.end(); ++it) {
		std::pair<std::string, char> p = it->first;
		char ch = p.second;
		if (isupper(ch)) continue;
		std::cout << "delta(s0, ";
		if (p.second != NULL) 
			std::cout << p.first << ", " << p.second << ") = ";
		else 
			std::cout << p.first << ", " << "h0" << ") = ";

		std::set<std::string> rules = it->second;
		std::cout << "(s0, " << *rules.begin() << ")" << std::endl;
	}
}

/*
bool PushDownAutomaton::parseInput(std::string inputStr)
{
	std::stack<char> s;//магазин
	s.push(startSymbol);
	//запоминает последовательность состояний стека
	//используется, когда возникает необходимость возврата назад
	std::stack <std::string> stackStatesSequence;
	stackStatesSequence.push(std::string(1, startSymbol));
	std::stack <std::string> inputStringStatesSequence;
	inputStringStatesSequence.push(inputStr);
	//использованые правила для нетерминального символа
	std::map<char, std::set<std::string>> usedRules;
	while (!inputStr.empty()) {

		if (s.empty()) {
			//придется возвращаться назад
			inputStringStatesSequence.pop();
			stackStatesSequence.pop();
			std::string lastStackState = stackStatesSequence.top();
			stackStatesSequence.pop();
			for (char c : lastStackState) {
				s.push(c);
			}
		}

		char symbol = s.top();//считываем символ с вспомогательной ленты
		s.pop();//удаляем его
		std::string rule = "";
		if (isupper(symbol)) { //если это нетерминальный символ
			//узнаем уже использованные для него правила
			std::set<std::string> currSymbolUsedRules = usedRules[symbol];
			auto p = std::make_pair("lambda", symbol);
			std::set<std::string> rulesForCurrSymbol = commands[p];
			std::set<std::string>::iterator it;
			for (it = rulesForCurrSymbol.begin(); it != rulesForCurrSymbol.end(); ++it) {
				rule = *it;
				//если это правило еще не было использовано
				if (currSymbolUsedRules.find(rule) == currSymbolUsedRules.end()) {
					currSymbolUsedRules.insert(rule);
					usedRules[symbol] = currSymbolUsedRules;
					break;
				}
			}
			if (it == rulesForCurrSymbol.end()) rule = "";
			//если было найдено неиспользованное правило
			if (!rule.empty()) {
				currSymbolUsedRules.insert(rule);
				usedRules[symbol] = currSymbolUsedRules;
				//std::cout << "(s0, " << inputStr << ", h0" << rule << ")";
				for (int i = 0; i < rule.length(); ++i) { //помещаем символы правила в стек
					s.push(rule[i]);
				}
			} //если все правила были использованы
			else {
				s = std::stack<char>();//очищаем стек
				//придется возвращаться назад
				inputStringStatesSequence.pop();
				stackStatesSequence.pop();
				std::string lastStackState = stackStatesSequence.top();
				stackStatesSequence.pop();
				for (char c : lastStackState) {
					s.push(c);
				}
			}
		}
		else { //иначе терминальный символ
			std::map<std::pair<std::string, char>, std::set<std::string>>::iterator it;
			//пытаемся найти для него сет
			it = commands.find(std::make_pair(std::string(1, inputStr[0]), symbol));
			if (it == commands.end()) {
				s = std::stack<char>();//очищаем стек
				//придется возвращаться назад
				inputStringStatesSequence.pop();
				stackStatesSequence.pop();
				std::string lastStackState = stackStatesSequence.top();
				stackStatesSequence.pop();
				for (char c : lastStackState) {
					s.push(c);
				}
			}
			else {
				inputStr.erase(0, 1);
				//s.pop();
			}
		}
		//std::cout << " |- ";
		std::stack<char> s2(s);
		std::string stackState = "";
		while (!s2.empty()) {//запоминаем состояние стека	
			stackState += s2.top();
			s2.pop();
		}
		stackStatesSequence.push(stackState);
		inputStringStatesSequence.push(inputStr);
	}
	printConfigSequence(stackStatesSequence, inputStringStatesSequence);
	return true;
}
*/

bool PushDownAutomaton::parseInput(std::string inputStr) {

	std::stack<char> s;//магазин
	s.push(startSymbol);
	//запоминает последовательность состояний стека
	//для последующего вывода последовательности конфигураций
	std::vector <std::string> statesSequence;
	statesSequence.push_back(std::string(1, startSymbol));
	std::vector <std::string> inputStringStatesSequence;
	inputStringStatesSequence.push_back(inputStr);

	//находим для него все правила
	auto p = std::make_pair("lambda", startSymbol);
	std::set<std::string> rulesForCurrSymbol = commands[p];
	//последовательно запускаем все пути
	for (auto it = rulesForCurrSymbol.begin(); it != rulesForCurrSymbol.end(); ++it) {
		std::string rule = *it;
		//запускаем для каждого ветвления выполнение
		bool success = process(s, statesSequence, inputStringStatesSequence, rule);
		if (success) {
			return true;//остальные пути не обрабатываем
		}
	}
	return false;
}

bool PushDownAutomaton::process(std::stack<char> s, 
	std::vector<std::string> &statesSequence,
	std::vector<std::string> &inputStringStatesSequence, std::string rule) {

	//берем очередное правило и записываем его в стек вместо верхнего символа
	s.pop();
	for (char c : rule) {
		s.push(c);
	}
	std::stack<char> s2(s);
	std::string currStackState = "";
	while (!s2.empty()) {
		currStackState += s2.top();
		s2.pop();
	}//сохраняем текущее состояние стека
	if (currStackState.compare("a+T+T") == 0) {
		int b = 0;
	}
	statesSequence.push_back(currStackState);
	inputStringStatesSequence.push_back(inputStringStatesSequence.back());
	//чтобы не происходило зацикливание алгоритма
	if (s.size() > inputStringStatesSequence.back().length()) {
		int a = 0;
		return false;
	}

	char topSymbol = s.top();//теперь берем символ с верха
	if (isupper(topSymbol)) { //если это нетерминальный символ
		//находим для него все правила
		/*auto p = std::make_pair("lambda", topSymbol);
		std::set<std::string> rulesForCurrSymbol = commands[p];
		//последовательно запускаем все пути
		for (auto it = rulesForCurrSymbol.begin(); it != rulesForCurrSymbol.end(); ++it) {
			std::string currRule = *it;
			std::vector<std::string> statesSequence2 = statesSequence;
			std::vector<std::string> inputStringStatesSequence2 = inputStringStatesSequence;
			if (process(s, statesSequence2, inputStringStatesSequence2, currRule)) {
				return true;//остальные пути не обрабатываем
			}
		}*/

		return processRulesForNonterminalSmbl(topSymbol, statesSequence, inputStringStatesSequence, s);
	}
	else { //иначе терминальный символ
		do {
			//находим для него правило
			if (currStackState.compare("a+T+T") == 0) {
				int b = 0;
			}
			std::string inputStr = inputStringStatesSequence.back();
			char inputSymbol = inputStr[0];
			auto p = std::make_pair(std::string(1, inputStr[0]), topSymbol);
			auto it = commands.find(p);
			if (it == commands.end()) return false;//если правило не найдено
			//если найдено, переходим к новой конфигурации
			s.pop();//стираем верхний символ
			std::string newInputStr = inputStr.erase(0, 1);//стираем первый элемент

			if (inputStr.empty() && s.empty()) { //здесь заканчивается алгоритм
				//TODO выводим последовательность
				int a = 0;
				return true;
			}
			if (s.empty()) { //если только стек оказался пустым
				return false;
			}
			//TODO вынести в отдельную функцию
			std::stack<char> s2(s);
			std::string currStackState = "";
			while (!s2.empty()) {
				currStackState += s2.top();
				s2.pop();
			}//сохраняем текущее состояние стека
			statesSequence.push_back(currStackState);
			inputStringStatesSequence.push_back(newInputStr);
			topSymbol = s.top();//берем новый верхний элемент
			//s.pop();
		} while (!isupper(topSymbol)); 	//пока не встретим снова нетерминальный символ
		//Встретили снова терминальный 
		return processRulesForNonterminalSmbl(topSymbol, statesSequence, inputStringStatesSequence, s);
	}

}

bool PushDownAutomaton::processRulesForNonterminalSmbl(char topSymbol,
	std::vector<std::string> statesSequence,
	std::vector<std::string> inputStringStatesSequence,
	std::stack<char> s) {
	//находим для него все правила
	auto p = std::make_pair("lambda", topSymbol);
	std::set<std::string> rulesForCurrSymbol = commands[p];
	//последовательно запускаем все пути
	for (auto it = rulesForCurrSymbol.begin(); it != rulesForCurrSymbol.end(); ++it) {
		std::string currRule = *it;
		//TODO убрать копирование???
		std::vector<std::string> statesSequence2 = statesSequence;
		std::vector<std::string> inputStringStatesSequence2 = inputStringStatesSequence;
		if (process(s, statesSequence2, inputStringStatesSequence2, currRule)) {
			//TODO выводим цепочку
			return true;//остальные пути не обрабатываем
		}
	}
	return false;
}

void PushDownAutomaton::printConfigSequence(std::stack<std::string> stackStatesSequence,
	std::stack<std::string> inputStringStatesSequence)
{
	while (!stackStatesSequence.empty()) {
		std::string stackState = stackStatesSequence.top();
		stackStatesSequence.pop();
		std::string inputStrState = inputStringStatesSequence.top();
		inputStringStatesSequence.pop();
		std::cout << "(s0, " << inputStrState << ", h0" << stackState << ") |-";
	}
}

