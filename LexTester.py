import os
# This file is used to run the Fibonacci search, ternary search, and binary search timers. It creates the input file (lexfile.txt) and then runs the timer.
# directories may need to change depending on where last year's work has been saved

# timer call functions
def LexicalTimer(list):
    for x in range(len(list)-1):
        file = open("input.txt","w")
        file.write(list[x])
        file.close()
        os.system("java -cp target/lexicographic-1.0-SNAPSHOT-jar-with-dependencies.jar mytweety.lexicographic.LexicalTimer")

def TernaryTimer(list):
    for x in range(len(list)-1):
        file = open("input.txt","w")
        file.write(list[x])
        file.close()
        os.system("java -cp target/mytweetyapp-1.0-SNAPSHOT-jar-with-dependencies.jar mytweety.mytweetyapp.benchTernary")

def BinaryTimer(list):
    for x in range(len(list)-1):
        file = open("input.txt","w")
        file.write(list[x])
        file.close()
        os.system("java -cp target/mytweetyapp-1.0-SNAPSHOT-jar-with-dependencies.jar mytweety.mytweetyapp.benchBinary")

# reads statement text file for test cases
# text file name is changed based on which knowledge base is being used
file = open("200_uniform.txt","r")
list = []
list = file.read().split("\n")
list = [x for x in list if not "." in x]
file.close()
ante = []
file = open("lexfile.txt","w")
for y in range(0,51,7):
    for x in list:
        if "~>" in x:
            pos = x.index("~>")
            if str(y) == x[0:pos]:
                if x[0:pos] not in ante:
                    ante.append(x[0:pos])
                    file.write(x+"\n")
file.close()

file = open("lexfile.txt","r")
list = file.read().split("\n")
file.close()

# calls timer functions
# depending on which timers are needed, the unused ones are commented out
LexicalTimer(list)
TernaryTimer(list)
BinaryTimer(list)