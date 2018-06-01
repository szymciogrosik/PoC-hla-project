package ieee1516e.constants;

public class ConfigConstants {
    public static final String READY_TO_RUN =           "ReadyToRun";
    public static final String FEDERATION_FILE_PATH =   "ShopFom.xml";
    public static final String FEDERATION_NAME =        "SklepFederation";
    public static final String CLIENT_FED =             "KlientFederate";
    public static final String CASH_REGISTER_FED =      "KasaFederate";
    public static final String QUEUE_FED =              "KolejkaFederate";
    public static final String MANAGER_FED =            "ManagerFederate";
    public static final String STATISTIC_FED =          "StatystykaFederate";

    public static final String QUEUE_NUMBER_NAME =          "numer_kolejki";
    public static final String QUEUE_LENGTH_NAME =          "dlugosc_kolejki";
    public static final String AMOUNT_OF_ARTICLES_NAME =    "liczba_zakupow";
    public static final String CASH_REGISTER_NUMBER_NAME =  "numer_kasy";
    public static final String CASH_REGISTER_IS_FREE_NAME = "czy_wolna";
    public static final String CLIENT_NUMBER_NAME =         "numer_klienta";

    private static final String OBJ_ROOT_NAME = "HLAobjectRoot.";
    public static final String CASH_REGISTER_OBJ_NAME = OBJ_ROOT_NAME + "Kasa";
    public static final String QUEUE_OBJ_NAME = OBJ_ROOT_NAME + "Kolejka";

    private static final String INTERACTION_ROOT_NAME = "HLAinteractionRoot.";
    public static final String JOIN_CLIENT_TO_QUEUE_INTERACTION_NAME = INTERACTION_ROOT_NAME + "Dolaczenie_klienta_do_kolejki";
    public static final String START_HANDLING_CLIENT_INTERACTION_NAME = INTERACTION_ROOT_NAME + "Rozpocznij_obsluge_klienta";
    public static final String OPEN_NEW_CASH_REGISTER_INTERACTION_NAME = INTERACTION_ROOT_NAME + "Otworz_nowa_kase";
}