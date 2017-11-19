package bot.MnSpainBot;

import org.junit.Test;

import bot.PersistentValues;

public class PersistentValuesTest {

	PersistentValues pv = new PersistentValues();
	@Test
	public void getId () {
		Integer idValue = pv.getIdValue();
		idValue += +1 ;
		pv.saveIdValue(idValue);
		System.out.println("Despues de aumentarlo => "+pv.getIdValue());		
	}
}
