package PokeData;

public class Pair<L,R> {
    private L l;
    private R r;
    public Pair(L l, R r){
        this.l = l;
        this.r = r;
    }
    public L getMove(){ return l; }
    public R getPP(){ return r; }
    public void setMove(L l){ this.l = l; }
    public void setPP(R r){ this.r = r; }
}
