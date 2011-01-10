

run: JavaLander.jar
	start view.htm

JavaLander.jar: JavaLander.class
	jar cvf JavaLander.jar *.class

JavaLander.class: JavaLander.java
	javac JavaLander.java

clean:
	rm -f *.class
	
