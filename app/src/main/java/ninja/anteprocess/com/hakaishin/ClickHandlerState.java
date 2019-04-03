package ninja.anteprocess.com.hakaishin;

/**
 * Created by michael on 2017/03/30.
 *
 * This class is to check if the button has been clicked
 */

 class ClickHandleState {

    public enum State{

        ON{
            void proceed(ClickHandleState entity) {
                entity.state = OFF;
            }

        },
        OFF{
            void proceed(ClickHandleState entity) {
                entity.state = ON;
            }

        },;

        abstract void proceed(ClickHandleState entity);
    }

    private State state = State.ON;

    public State getClickState() {
        return state;
    }

    public void setClickState(State state) {
        this.state = state;
    }

    public void proceed(){
        this.state.proceed(this);
    }

    private static void sendNotifcation(ClickHandleState entity){}

}