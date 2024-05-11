import java.util.Random;

public class Autoassociator {
	private int weights[][];
	private int trainingCapacity;

	public Autoassociator(CourseArray courses) {
		int numCourses = courses.length() - 1; // Subtracting 1 to exclude the dummy course
		weights = new int[numCourses + 1][numCourses + 1]; // Adding 1 for the dummy course

		// Initialize weights randomly
		Random rand = new Random();
		for (int i = 1; i <= numCourses; i++) {
			for (int j = 1; j <= numCourses; j++) {
				weights[i][j] = rand.nextBoolean() ? 1 : -1; // Randomly assign weights as +1 or -1
			}
		}

		// Set training capacity
		trainingCapacity = numCourses;
	}

	public int getTrainingCapacity() {
		return trainingCapacity;
	}

	public void training(int pattern[]) {
		// Update weights based on the input pattern
		for (int i = 1; i < pattern.length; i++) {
			for (int j = 1; j < pattern.length; j++) {
				if (i != j) {
					weights[i][j] += pattern[i] * pattern[j];
				}
			}
		}
	}

	public int unitUpdate(int neurons[]) {
		Random rand = new Random();
		int index = rand.nextInt(neurons.length - 1) + 1; // Randomly select a neuron to update (excluding dummy)
		int sum = 0;

		// Calculate the weighted sum of inputs to the selected neuron
		for (int i = 1; i < neurons.length; i++) {
			sum += weights[index][i] * neurons[i];
		}

		// Apply activation function (signum function)
		neurons[index] = sum >= 0 ? 1 : -1;

		return index;
	}

	public void unitUpdate(int neurons[], int index) {
		int sum = 0;

		// Calculate the weighted sum of inputs to the specified neuron
		for (int i = 1; i < neurons.length; i++) {
			sum += weights[index][i] * neurons[i];
		}

		// Apply activation function (signum function)
		neurons[index] = sum >= 0 ? 1 : -1;
	}

	public void chainUpdate(int neurons[], int steps) {
		for (int i = 0; i < steps; i++) {
			int index = unitUpdate(neurons);
			unitUpdate(neurons, index); // Update the selected neuron
		}
	}

	public void fullUpdate(int neurons[]) {
		boolean converged = false;
		int maxIterations = 1000; // Maximum iterations to prevent infinite loops

		for (int iter = 0; iter < maxIterations && !converged; iter++) {
			int[] prevNeurons = neurons.clone(); // Save current state for convergence check
			int index = unitUpdate(neurons);
			unitUpdate(neurons, index); // Update the selected neuron

			// Check for convergence by comparing with the previous state
			converged = true;
			for (int i = 1; i < neurons.length; i++) {
				if (neurons[i] != prevNeurons[i]) {
					converged = false;
					break;
				}
			}
		}
	}
}
