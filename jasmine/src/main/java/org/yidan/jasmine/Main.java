package org.yidan.jasmine;

public class Main implements Logger{
	public static void main(String[] args){
		if(args == null || args.length == 0){
			System.out.println("Please specify the jasmine properties file path");
			return;
		}
		Main shell = new Main();
		Generator generator = new Generator(shell);
		generator.generate(args[0]);

	}

	@Override
	public void error(Exception e) {
		e.printStackTrace();
	}

	@Override
	public void error(String message) {
		System.err.println(message);
	}

	@Override
	public void info(String message) {
		System.out.println(message);
	}

	@Override
	public void setProgress(int percent) {
		System.out.print(percent + "% has generated...\r");
//		System.out.print("\r");
	}
}
