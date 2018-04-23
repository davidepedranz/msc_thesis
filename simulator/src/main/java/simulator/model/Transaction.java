package simulator.model;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Transaction {

	public static Transaction create(int id, int from, int to, int amount) {
		return new AutoValue_Transaction(id, from, to, amount);
	}

	public abstract int id();

	public abstract int from();

	public abstract int to();

	// expressed in currency units (smallest part of the currency)
	public abstract int amount();
}
