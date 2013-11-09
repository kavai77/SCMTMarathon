package net.himadri.scmt.server;

public class NotExistingRunnerException extends Exception {
    public NotExistingRunnerException(String raceNumber, Long versenyId) {
        super(String.format("Not existing runner with raceNumber %s and versenyId %d", raceNumber, versenyId));
    }
}
