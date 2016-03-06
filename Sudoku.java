//  Steven Malis
//  smmalis37@gmail.com

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

public class Sudoku extends JFrame implements KeyListener, ActionListener, Runnable
{

	public static void main(String args[])
	{
		new Sudoku();
	}

	private boolean running = false;
	private JTextField[][] textBoxes;
	private JButton clearButton, resetButton, solveButton, infoButton;
	private Color solvedColor = new Color(0, 127, 0);

	public Sudoku()
	{
		super("Steven's SuDoKu Solver");

		textBoxes = new JTextField[9][9];
		JPanel centerPanel = new JPanel(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridy = 0;
		constraints.gridx = 3;
		centerPanel.add(makeSeparator(), constraints);
		constraints.gridx = 7;
		centerPanel.add(makeSeparator(), constraints);
		constraints.gridx = 0;
		constraints.gridy = 3;
		centerPanel.add(makeSeparator(), constraints);
		constraints.gridy = 7;
		centerPanel.add(makeSeparator(), constraints);

		for (int row = 0; row < 9; row++)
			for (int col = 0; col < 9; col++)
			{
				textBoxes[row][col] = new JTextField();
				textBoxes[row][col].setFont(new Font("Arial", Font.BOLD, 50));
				textBoxes[row][col].setHorizontalAlignment(JTextField.CENTER);
				textBoxes[row][col].addKeyListener(this);
				textBoxes[row][col].setMinimumSize(new Dimension(50, 50));

				constraints.gridx = col + (col / 3);
				constraints.gridy = row + (row / 3);
				centerPanel.add(textBoxes[row][col], constraints);
			}

		JPanel buttonPanel = new JPanel(new GridLayout(1, 3));

		infoButton = new JButton("How to use");
		infoButton.addActionListener(this);
		infoButton.addKeyListener(this);
		clearButton = new JButton("Clear");
		clearButton.addActionListener(this);
		clearButton.addKeyListener(this);
		resetButton = new JButton("Reset");
		resetButton.addActionListener(this);
		resetButton.addKeyListener(this);
		solveButton = new JButton("Solve");
		solveButton.addActionListener(this);
		solveButton.addKeyListener(this);

		buttonPanel.add(infoButton);
		buttonPanel.add(clearButton);
		buttonPanel.add(resetButton);
		buttonPanel.add(solveButton);

		centerPanel.setBackground(Color.BLACK);

		add(centerPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		setSize(450, 500);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setResizable(false);
		setVisible(true);
	}

	private JSeparator makeSeparator()
	{
		JSeparator separate = new JSeparator();
		separate.setForeground(Color.BLACK);
		return separate;
	}

	public void keyTyped(KeyEvent e)
	{
		if ((e.getKeyChar() < '1') || (e.getKeyChar() > '9') || running)
			e.setKeyChar(KeyEvent.CHAR_UNDEFINED);
		else
		{
			((JTextField) (e.getSource())).setText("");
			((JTextField) (e.getSource())).setForeground(Color.BLACK);
		}
	}

	public void keyPressed(KeyEvent e)
	{
		FocusTraversalPolicy order = getFocusTraversalPolicy();
		Component willGainFocus = getFocusOwner();
		int backSpaces = 0, forwardSpaces = 0;

		if (e.getKeyCode() == KeyEvent.VK_LEFT)
			backSpaces = 1;
		else if (e.getKeyCode() == KeyEvent.VK_UP)
			backSpaces = 9;
		else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
			forwardSpaces = 1;
		else if (e.getKeyCode() == KeyEvent.VK_DOWN)
			forwardSpaces = 9;
		else if (e.getKeyCode() == KeyEvent.VK_F1)
			infoButton.doClick();
		else if (e.getKeyCode() == 10 && !running)
			solveButton.doClick();

		for (int goingBack = 0; goingBack < backSpaces; goingBack++)
		{
			willGainFocus = order.getComponentBefore(this, willGainFocus);
			if (willGainFocus instanceof JButton)
				goingBack--;
		}
		for (int goingForward = 0; goingForward < forwardSpaces; goingForward++)
		{
			willGainFocus = order.getComponentAfter(this, willGainFocus);
			if (willGainFocus instanceof JButton)
				goingForward--;
		}

		willGainFocus.requestFocusInWindow();
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == clearButton && !running)
			clearAll();
		else if (e.getSource() == resetButton && !running)
			clearSolved();
		else if (e.getSource() == solveButton && !running)
			new Thread(this).start();
		else if (e.getSource() == infoButton && !running)
			JOptionPane.showMessageDialog(this, "Enter your puzzle into the grid boxes using the number keys.  When complete, press the solve button.\nYour puzzle entries will appear in black and the solution will appear in green.\nPress the Reset button to erase the solution and view your puzzle entries.\nPress the Clear button to erase the whole puzzle and start over.", "Instuctions for sudoku puzzle solver", JOptionPane.INFORMATION_MESSAGE);
	}

	private void clearAll()
	{
		for (int row = 0; row < 9; row++)
			for (int col = 0; col < 9; col++)
				textBoxes[row][col].setText("");
	}

	private void clearSolved()
	{
		for (int row = 0; row < 9; row++)
			for (int col = 0; col < 9; col++)
				if (textBoxes[row][col].getForeground().equals(solvedColor))
					textBoxes[row][col].setText("");
	}

	public void run()
	{
		running = true;

		setAllEnabled(false);
		setAllCursors(new Cursor(Cursor.WAIT_CURSOR));

		int[][] numGrid = textGridToIntGrid();

		if (validate(numGrid))
		{
			logicSolve(numGrid);

			if (bruteForce(0, 0, numGrid))
				IntGridToTextGrid(numGrid);
			else
				JOptionPane.showMessageDialog(this, "No solution exists for this puzzle.", "Impossible", JOptionPane.ERROR_MESSAGE);
		}
		else
			JOptionPane.showMessageDialog(this, "Your entries do not follow the rules for a valid Sudoku puzzle.  Please fix your error.", "Invalid entry", JOptionPane.ERROR_MESSAGE);

		setAllEnabled(true);
		setAllCursors(new Cursor(Cursor.DEFAULT_CURSOR));
		running = false;
	}

	private void setAllEnabled(boolean entryStatus)
	{
		for (int row = 0; row < 9; row++)
			for (int col = 0; col < 9; col++)
				textBoxes[row][col].setEditable(entryStatus);

		infoButton.setEnabled(entryStatus);
		clearButton.setEnabled(entryStatus);
		resetButton.setEnabled(entryStatus);
		solveButton.setEnabled(entryStatus);
	}

	private void setAllCursors(Cursor waitOrDefault)
	{
		setCursor(waitOrDefault);
		for (int row = 0; row < 9; row++)
			for (int col = 0; col < 9; col++)
				textBoxes[row][col].setCursor(waitOrDefault);
	}

	private int[][] textGridToIntGrid()
	{
		int[][] numGrid = new int[9][9];
		for (int row = 0; row < 9; row++)
			for (int col = 0; col < 9; col++)
				numGrid[row][col] = textToInt(textBoxes[row][col].getText());
		return numGrid;
	}

	private int textToInt(String text)
	{
		if (text.equals(""))
			return 0;
		else
			return Integer.parseInt(text);
	}

	private void IntGridToTextGrid(int[][] numGrid)
	{
		for (int row = 0; row < 9; row++)
			for (int col = 0; col < 9; col++)
				if (textBoxes[row][col].getText().equals(""))
				{
					textBoxes[row][col].setText(IntToText(numGrid[row][col]));
					textBoxes[row][col].setForeground(solvedColor);
				}
	}

	private String IntToText(int num)
	{
		if (num == 0)
			return "";
		else
			return Integer.toString(num);
	}

	private boolean isOkayHere(int[][] numGrid, int row, int col, int numCheck)
	{
		for (int check = 0; check < 9; check++)
			if ((numGrid[row][check] == numCheck) || (numGrid[check][col] == numCheck))
				return false;

		for (int rowcheck = (row / 3) * 3; rowcheck < ((row / 3) + 1) * 3; rowcheck++)
			for (int colcheck = (col / 3) * 3; colcheck < ((col / 3) + 1) * 3; colcheck++)
				if (rowcheck != row || colcheck != col)
					if (numGrid[rowcheck][colcheck] == numCheck)
						return false;
		return true;
	}

	private boolean validate(int[][] numGrid)
	{
		for (int row = 0; row < 9; row++)
			for (int col = 0; col < 9; col++)
				if (numGrid[row][col] != 0)
				{
					int temp = numGrid[row][col];
					numGrid[row][col] = 0;
					if (!isOkayHere(numGrid, row, col, temp))
						return false;
					numGrid[row][col] = temp;
				}
		return true;
	}

	private void logicSolve(int[][] numGrid)
	{
		boolean found;
		int numFound = 0;
		for (int row = 0; row < 9; row++)
			for (int col = 0; col < 9; col++)
				if (numGrid[row][col] == 0)
				{
					found = false;
					for (int numCheck = 1; numCheck < 10; numCheck++)
						if (isOkayHere(numGrid, row, col, numCheck))
						{
							if (found)
							{
								found = false;
								numCheck = 10;
							}
							else
							{
								found = true;
								numFound = numCheck;
							}
						}
					if (found)
					{
						numGrid[row][col] = numFound;
						row = 0;
						col = -1;
					}
				}
	}

	private boolean bruteForce(int row, int col, int[][] numGrid)
	{
		if (col == 9)
		{
			col = 0;
			row++;
		}
		if (row == 9)
			return true;

		if (numGrid[row][col] != 0)
			return bruteForce(row, col + 1, numGrid);

		for (int numCheck = 1; numCheck < 10; numCheck++)
			if (isOkayHere(numGrid, row, col, numCheck))
			{
				numGrid[row][col] = numCheck;
				if (bruteForce(row, col + 1, numGrid))
					return true;
				numGrid[row][col] = 0;
			}
		return false;
	}

	public void keyReleased(KeyEvent e)
	{
	}
}
