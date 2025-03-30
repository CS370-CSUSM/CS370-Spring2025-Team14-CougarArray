package Cryptography;

public class Keys {
    private String privateKey;
    private String publicKey;

    Keys(String privateKey, String publicKey) {
        setPrivate(privateKey);
        setPublic(publicKey);
    }

    //note that the setters are private.
    //The reason being is that private key and public key should never be modified in any other package
    //and if any modification is needed it needs to be done first to the Config file THEN to here
    //This is better practice to ensure certain variables are not modified during runtime
    private void setPrivate(String privateKey) {
        this.privateKey = privateKey;
    }

    private void setPublic(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivate() {
        return this.privateKey;
    }

    public String getPublic() {
        return this.publicKey;
    }
}