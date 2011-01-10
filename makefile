

run: JavaLander.class
	start view.htm

JavaLander.class: JavaLander.java
	javac JavaLander.java

clean:
	rm -f *.class
	
