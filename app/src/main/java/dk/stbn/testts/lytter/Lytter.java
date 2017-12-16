package dk.stbn.testts.lytter;

/**
 * Created by sune on 12/7/17.
 */

public interface  Lytter {

    void grunddataHentet();
    void logOpdateret();
    void netværksændring(boolean forbundet);

}
