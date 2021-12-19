package nereus.db.objects;

public class ItemEffect {
    public int itemid;
    public double damageincrease,damagetaken,ExtraExp,extragold,extracoins,extrarep,haste,dodge,hit,crit;
    public ItemEffect() {
        super();
    }

    public int getItemid() {
        return itemid;
    }

    public double getDamageincrease() {
        return damageincrease;
    }

    public double getDamageTaken() {
        return damagetaken;
    }

    public double getExtraExp() {
        return ExtraExp;
    }

    public double getExtragold() {
        return extragold;
    }

    public double getExtracoins() {
        return extracoins;
    }

    public double getExtrarep() {
        return extrarep;
    }

    public double getHaste() {
        return haste;
    }

    public double getDodge() {
        return dodge;
    }

    public double getHit() {
        return hit;
    }

    public double getCrit() {
        return crit;
    }
}
