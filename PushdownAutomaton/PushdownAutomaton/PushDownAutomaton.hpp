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
	std::set<char> p;//������ ������������ �������
	std::set<char> z;
	char startSymbol;
	//{{������� ������, ������� ������ �����}, ������ ���������}
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

		//TODO ��������� ������ �� ������������ �������

		char nonTermSymbol = line[0];
		if (firstLine) {//���� ��� ������ ������
			startSymbol = nonTermSymbol;
			firstLine = false;
		}
		z.insert(nonTermSymbol);//����� ������ �������������� ������, ��� ��� ��� �� ����������
		std::string rightSide = line.substr(2, line.length() - 2);//����� ������ �����
		std::string rule;
		std::set<std::string> currRules;//������� ��� �������� �������
		size_t posOfDelimeter = rightSide.find('|');
		if (posOfDelimeter == std::string::npos) {//���� � ������ ������ ���� �������
			std::reverse(rightSide.begin(), rightSide.end());//�������� ���������
			currRules.insert(rightSide);//��������� ��� ������ �����
			insertSymbolToSets(rightSide);
		}
		else do {
			rule = rightSide.substr(0, posOfDelimeter);
			std::reverse(rule.begin(), rule.end());//�������� ���������
			insertSymbolToSets(rule);
			currRules.insert(rule);//��������� ��� � ������
			rightSide.erase(0, posOfDelimeter + 1);
			//} while ((posOfDelimeter = rightSide.find('|')) != std::string::npos);
		} while (rightSide.length() != 0);
		//���������� ������� ���� 1
		std::pair<std::string, char> p = std::make_pair("lambda", nonTermSymbol);
		commands.insert(std::make_pair(p, currRules));

	}

	createSecondTypeCommands();//���������� ������� ������� ����
	//��������� �������
	std::set<std::string> rule = { "lambda" };
	//������� NULL ��� �������� �� ��� �����
	std::pair<std::string, char> p = std::make_pair("lambda", NULL);
	commands.insert(std::make_pair(p, rule));

}

void PushDownAutomaton::insertSymbolToSets(std::string str) 
{
	for (int i = 0; i < str.length(); ++i) { //���������� �� ������, ����� 
												   //��������� ���������
		char ch = str[i];
		z.insert(ch);
		if (!isupper(ch)) p.insert(ch);
	}
}

void PushDownAutomaton::createSecondTypeCommands() {

	//������� ��� ������������ �������
	std::set<char>::iterator it;
	//for (it = z.begin(); it != z.end(); ++it) {
	for (char ch : z) {
		//char ch = *it;
		if (isupper(ch)) { //�������������� - ������ ��������� 
			continue;
		}
		//���������� �������
		std::pair<std::string, char> p = std::make_pair(std::string(1, ch), ch);
		std::set<std::string> currRules = { "lambda" };
		commands.insert(std::make_pair(p, currRules));
	}

}

void PushDownAutomaton::print() 
{
	//�������� ��������� Z, P
	std::cout << "P = {";
	printSet(p);
	std::cout << "}" << std::endl;
	std::cout << "Z = {";
	printSet(z);
	std::cout << ", h0}" << std::endl;

	printCommands();//�������� �������
}

void PushDownAutomaton::printSet(std::set<char> s)
{
	std::set<char>::iterator it;
	it = s.begin();
	std::cout << *it;//�������� ������ ������
	for (++it; it != s.end(); ++it) {
		std::cout << ", " << *it;
	}
}

void PushDownAutomaton::printCommands()
{
	std::map<std::pair<std::string, char>, std::set<std::string>>::iterator it;
	//������� �������� ��� �������������� �������
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
		std::cout << "(s0, " << *set_it << ")";//�������� ������ �������
		for (++set_it; set_it != rules.end(); ++set_it) {
			std::cout << "; (s0, " << *set_it << ")";
		}
		std::cout << "}" << std::endl;
	}


	//������ ��� ������������
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
	std::stack<char> s;//�������
	s.push(startSymbol);
	//���������� ������������������ ��������� �����
	//������������, ����� ��������� ������������� �������� �����
	std::stack <std::string> stackStatesSequence;
	stackStatesSequence.push(std::string(1, startSymbol));
	std::stack <std::string> inputStringStatesSequence;
	inputStringStatesSequence.push(inputStr);
	//������������� ������� ��� ��������������� �������
	std::map<char, std::set<std::string>> usedRules;
	while (!inputStr.empty()) {

		if (s.empty()) {
			//�������� ������������ �����
			inputStringStatesSequence.pop();
			stackStatesSequence.pop();
			std::string lastStackState = stackStatesSequence.top();
			stackStatesSequence.pop();
			for (char c : lastStackState) {
				s.push(c);
			}
		}

		char symbol = s.top();//��������� ������ � ��������������� �����
		s.pop();//������� ���
		std::string rule = "";
		if (isupper(symbol)) { //���� ��� �������������� ������
			//������ ��� �������������� ��� ���� �������
			std::set<std::string> currSymbolUsedRules = usedRules[symbol];
			auto p = std::make_pair("lambda", symbol);
			std::set<std::string> rulesForCurrSymbol = commands[p];
			std::set<std::string>::iterator it;
			for (it = rulesForCurrSymbol.begin(); it != rulesForCurrSymbol.end(); ++it) {
				rule = *it;
				//���� ��� ������� ��� �� ���� ������������
				if (currSymbolUsedRules.find(rule) == currSymbolUsedRules.end()) {
					currSymbolUsedRules.insert(rule);
					usedRules[symbol] = currSymbolUsedRules;
					break;
				}
			}
			if (it == rulesForCurrSymbol.end()) rule = "";
			//���� ���� ������� ���������������� �������
			if (!rule.empty()) {
				currSymbolUsedRules.insert(rule);
				usedRules[symbol] = currSymbolUsedRules;
				//std::cout << "(s0, " << inputStr << ", h0" << rule << ")";
				for (int i = 0; i < rule.length(); ++i) { //�������� ������� ������� � ����
					s.push(rule[i]);
				}
			} //���� ��� ������� ���� ������������
			else {
				s = std::stack<char>();//������� ����
				//�������� ������������ �����
				inputStringStatesSequence.pop();
				stackStatesSequence.pop();
				std::string lastStackState = stackStatesSequence.top();
				stackStatesSequence.pop();
				for (char c : lastStackState) {
					s.push(c);
				}
			}
		}
		else { //����� ������������ ������
			std::map<std::pair<std::string, char>, std::set<std::string>>::iterator it;
			//�������� ����� ��� ���� ���
			it = commands.find(std::make_pair(std::string(1, inputStr[0]), symbol));
			if (it == commands.end()) {
				s = std::stack<char>();//������� ����
				//�������� ������������ �����
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
		while (!s2.empty()) {//���������� ��������� �����	
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

	std::stack<char> s;//�������
	s.push(startSymbol);
	//���������� ������������������ ��������� �����
	//��� ������������ ������ ������������������ ������������
	std::vector <std::string> statesSequence;
	statesSequence.push_back(std::string(1, startSymbol));
	std::vector <std::string> inputStringStatesSequence;
	inputStringStatesSequence.push_back(inputStr);

	//������� ��� ���� ��� �������
	auto p = std::make_pair("lambda", startSymbol);
	std::set<std::string> rulesForCurrSymbol = commands[p];
	//��������������� ��������� ��� ����
	for (auto it = rulesForCurrSymbol.begin(); it != rulesForCurrSymbol.end(); ++it) {
		std::string rule = *it;
		//��������� ��� ������� ��������� ����������
		bool success = process(s, statesSequence, inputStringStatesSequence, rule);
		if (success) {
			return true;//��������� ���� �� ������������
		}
	}
	return false;
}

bool PushDownAutomaton::process(std::stack<char> s, 
	std::vector<std::string> &statesSequence,
	std::vector<std::string> &inputStringStatesSequence, std::string rule) {

	//����� ��������� ������� � ���������� ��� � ���� ������ �������� �������
	s.pop();
	for (char c : rule) {
		s.push(c);
	}
	std::stack<char> s2(s);
	std::string currStackState = "";
	while (!s2.empty()) {
		currStackState += s2.top();
		s2.pop();
	}//��������� ������� ��������� �����
	if (currStackState.compare("a+T+T") == 0) {
		int b = 0;
	}
	statesSequence.push_back(currStackState);
	inputStringStatesSequence.push_back(inputStringStatesSequence.back());
	//����� �� ����������� ������������ ���������
	if (s.size() > inputStringStatesSequence.back().length()) {
		int a = 0;
		return false;
	}

	char topSymbol = s.top();//������ ����� ������ � �����
	if (isupper(topSymbol)) { //���� ��� �������������� ������
		//������� ��� ���� ��� �������
		/*auto p = std::make_pair("lambda", topSymbol);
		std::set<std::string> rulesForCurrSymbol = commands[p];
		//��������������� ��������� ��� ����
		for (auto it = rulesForCurrSymbol.begin(); it != rulesForCurrSymbol.end(); ++it) {
			std::string currRule = *it;
			std::vector<std::string> statesSequence2 = statesSequence;
			std::vector<std::string> inputStringStatesSequence2 = inputStringStatesSequence;
			if (process(s, statesSequence2, inputStringStatesSequence2, currRule)) {
				return true;//��������� ���� �� ������������
			}
		}*/

		return processRulesForNonterminalSmbl(topSymbol, statesSequence, inputStringStatesSequence, s);
	}
	else { //����� ������������ ������
		do {
			//������� ��� ���� �������
			if (currStackState.compare("a+T+T") == 0) {
				int b = 0;
			}
			std::string inputStr = inputStringStatesSequence.back();
			char inputSymbol = inputStr[0];
			auto p = std::make_pair(std::string(1, inputStr[0]), topSymbol);
			auto it = commands.find(p);
			if (it == commands.end()) return false;//���� ������� �� �������
			//���� �������, ��������� � ����� ������������
			s.pop();//������� ������� ������
			std::string newInputStr = inputStr.erase(0, 1);//������� ������ �������

			if (inputStr.empty() && s.empty()) { //����� ������������� ��������
				//TODO ������� ������������������
				int a = 0;
				return true;
			}
			if (s.empty()) { //���� ������ ���� �������� ������
				return false;
			}
			//TODO ������� � ��������� �������
			std::stack<char> s2(s);
			std::string currStackState = "";
			while (!s2.empty()) {
				currStackState += s2.top();
				s2.pop();
			}//��������� ������� ��������� �����
			statesSequence.push_back(currStackState);
			inputStringStatesSequence.push_back(newInputStr);
			topSymbol = s.top();//����� ����� ������� �������
			//s.pop();
		} while (!isupper(topSymbol)); 	//���� �� �������� ����� �������������� ������
		//��������� ����� ������������ 
		return processRulesForNonterminalSmbl(topSymbol, statesSequence, inputStringStatesSequence, s);
	}

}

bool PushDownAutomaton::processRulesForNonterminalSmbl(char topSymbol,
	std::vector<std::string> statesSequence,
	std::vector<std::string> inputStringStatesSequence,
	std::stack<char> s) {
	//������� ��� ���� ��� �������
	auto p = std::make_pair("lambda", topSymbol);
	std::set<std::string> rulesForCurrSymbol = commands[p];
	//��������������� ��������� ��� ����
	for (auto it = rulesForCurrSymbol.begin(); it != rulesForCurrSymbol.end(); ++it) {
		std::string currRule = *it;
		//TODO ������ �����������???
		std::vector<std::string> statesSequence2 = statesSequence;
		std::vector<std::string> inputStringStatesSequence2 = inputStringStatesSequence;
		if (process(s, statesSequence2, inputStringStatesSequence2, currRule)) {
			//TODO ������� �������
			return true;//��������� ���� �� ������������
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

