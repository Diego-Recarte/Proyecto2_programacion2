package Instagram;


public class instaController {
    private static instaController instance;
    private instaManager insta;
    
    private instaController(){}
    
    public static instaController getInstance(){
        if(instance==null){
            instance = new instaController();
        }
        
        return instance;
    }
    
    
    public void setInsta(instaManager insta){
        this.insta=insta;
    }
    
    public instaManager getInsta(){
        return this.insta;
    }
    
    
}
