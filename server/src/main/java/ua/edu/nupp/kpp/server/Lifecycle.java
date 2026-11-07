package ua.edu.nupp.kpp.server;
public final class Lifecycle {
    private Lifecycle() {}
    public static boolean allowed(RequestState from, RequestState to) {
        return (from == RequestState.CREATED && to == RequestState.APPROVED)
            || (from == RequestState.APPROVED && to == RequestState.ISSUED)
            || (from == RequestState.ISSUED && to == RequestState.RETURNED);
    }
    public static RequestState transition(RequestState from, RequestState to) {
        if (!allowed(from, to)) throw new IllegalArgumentException("Недопустимий перехід стану заявки");
        return to;
    }
}
