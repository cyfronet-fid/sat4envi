package pl.cyfronet.s4e.bean;

public enum OverlayOwner {
    /**
    * Available for each user
    */
    GLOBAL,

    /**
     * Created by user and for it's purposes only
     */
    PERSONAL,

    /**
     * Created by institution administrator
     * and available only for members of the institution
     */
    INSTITUTIONAL
}
