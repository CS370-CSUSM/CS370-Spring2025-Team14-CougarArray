package Cryptography;

//This is a subsytem that has both Encryption & Decryption in one place
public class CryptographyClient {

    public Encryption encryption;
    public Decrypytion decrypytion;

    public CryptographyClient() {
        Keys keys = Cryptography.generateKeys();

        encryption = new Encryption(keys.getPrivate(), keys.getPublic());
        decrypytion = new Decrypytion(keys.getPrivate(), keys.getPublic());
    }

    public static void main(String[] args) throws Exception {
        CryptographyClient testEngine = new CryptographyClient();
        testEngine.encryption.Encrypt("test.txt");   
        testEngine.decrypytion.Decrypt("test.txt", "testoutput.txt");
    }
    
}

//TODO
//Make it so where it asks to input "public key or private key", just replace it with Key class
//I been having headache because putting in the wrong key really does screw everything up
//i hate this.
