# lexicographic-closure
Lexicographic Closure implementation was built using Java 18 owing to the TweetyProject and JMH dependency.

[maven](https://maven.apache.org/users/index.html) is required to build the project.

To compile, from the `lexicographic-closure` directory, run

```sh
mvn compile
```

A **jar** file will be compiled to the `target` directory.

To run the reasoner app, from the `lexicographic-closure` diectory, run

```sh
java -cp target/lexicographic-1.0-SNAPSHOT-jar-with-dependencies.jar mytweety.lexicographic.App
```

This allows for the user to enter in the file name for the knowledge base.

To run the base rank algorithm and store the knowledge base as a json file, from the `lexicographic-closure` directory, run

```sh
java -cp target/lexicographic-1.0-SNAPSHOT-jar-with-dependencies.jar mytweety.lexicographic.fileWriter
```

This knowledge base is used for the testing classes listed below.

To run the JMH timer for the Fibonacci search optimisation, from the `lexicographic-closure` directory, run

```sh
java -cp target/lexicographic-1.0-SNAPSHOT-jar-with-dependencies.jar mytweety.lexicographic.LexicalTimer
```

To run the JMH timer for the power set optimisation, from the `lexicographic-closure` directory, run

```sh
java -cp target/lexicographic-1.0-SNAPSHOT-jar-with-dependencies.jar mytweety.lexicographic.PowerTimer
```

To run these timers multiple times, the below python files can be used.

To run the Fibonacci search, ternary search, and binary search timers, run

```sh
python LexTester.py
```

To run the power set timers, run

```sh
python PowerTester.py
```