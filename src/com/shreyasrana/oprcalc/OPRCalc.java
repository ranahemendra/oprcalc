package com.shreyasrana.oprcalc;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import Jama.Matrix;

public class OPRCalc {
	private double[][] matches;
	private double[][] scores;
	private Integer[] teamList;
	
	public Matrix getOPR(String fileName) throws IOException {
		readData(fileName);
		
		Matrix localMatches = new Matrix(matches);
		Matrix localScores = new Matrix(scores);
		
		localMatches.print(4,  0);
		System.out.println();
		
		localScores.print(4,  0);
		System.out.println();		

		// Get the transpose of matches
		Matrix matchesTranspose = localMatches.transpose();
		Matrix productLeft = matchesTranspose.times(localMatches);		
		Matrix productRight = matchesTranspose.times(localScores);		
		
		Matrix oprs = productLeft.solve(productRight);
		
		return oprs;
	}
	
	private void readData(String fileName) throws IOException {
		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
		
		Set<Integer> tl = new TreeSet<Integer>();
		
		ArrayList<int[]> dataAsList = new ArrayList<int[]>();
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			String[] tokens = line.split(" ");
			int [] intTokens = new int[tokens.length];
			for (int i = 0; i < tokens.length; i++) {
				intTokens[i] = Integer.parseInt(tokens[i]);
			}
			
			tl.add(intTokens[2]);
			tl.add(intTokens[3]);
			tl.add(intTokens[4]);
			tl.add(intTokens[5]);
			
			dataAsList.add(intTokens);
		}
		
		reader.close();
		
		teamList = tl.toArray(new Integer[0]);
		Map<Integer, Integer> teamListMap = new HashMap<Integer, Integer>();
		
		for (int i = 0; i < teamList.length; i++) {
			int team = teamList[i];
			teamListMap.put(team, i);
		}

		matches = new double[dataAsList.size() * 2][teamList.length];
		scores = new double[dataAsList.size() * 2][1];

		int row = 0;
		for (int[] data: dataAsList) {
			// 0th index will have score of first alliance
			// 1st index will have score of the second alliance
			// 2nd index will have first team in the first alliance
			// 3rd index will have second team in the first alliance
			// 4th index will have first team in the second alliance
			// 5th index will have second team in the second alliance.

			int score1 = data[0];				
			int alliance1Team1 = data[2];
			int alliance1Team2 = data[3];

			int team1Index = teamListMap.get(alliance1Team1);
			int team2Index = teamListMap.get(alliance1Team2); 
			matches[row][team1Index] = 1d;
			matches[row][team2Index] = 1d;
			scores[row][0] = (double) score1;

			row++;

			int score2 = data[1];
			int alliance2Team1 = data[4];
			int alliance2Team2 = data[5];

			matches[row][teamListMap.get(alliance2Team1)] = 1d;
			matches[row][teamListMap.get(alliance2Team2)] = 1d;
			scores[row][0] = (double) score2;
			row++;
		}
	}
	
	// Change the name of the scores file if you need to.
	private static final String SCORES_FILE 	= "resources/scores.csv";
	private static final String OPRS_FILE	= "resources/oprs.csv";
	
	public static void main(String[] args) throws IOException {
		OPRCalc calc = new OPRCalc();
		Matrix oprs = calc.getOPR(SCORES_FILE);
		Integer[] teamList = calc.teamList;
		
		double[][] oprsArr = oprs.getArray();
		
		PrintStream out = new PrintStream(OPRS_FILE);
		for (int i = 0; i < oprsArr.length; i++) {
			out.printf(teamList[i] + ",%.2f\n",oprsArr[i][0]);
		}
		out.close();
	}
}
