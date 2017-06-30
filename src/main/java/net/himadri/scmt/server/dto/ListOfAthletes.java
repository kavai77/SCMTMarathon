package net.himadri.scmt.server.dto;

import java.util.List;

public class ListOfAthletes {
    private final String raceName;
    private final boolean active;
    private final List<Athlete> athleteList;

    public ListOfAthletes(String raceName, boolean active, List<Athlete> athleteList) {
        this.raceName = raceName;
        this.active = active;
        this.athleteList = athleteList;
    }

    public String getRaceName() {
        return raceName;
    }

    public boolean isActive() {
        return active;
    }

    public List<Athlete> getAthleteList() {
        return athleteList;
    }

    public static class Athlete {
        private final String raceNumber;
        private final String name;
        private final String egyesulet;
        private final String korcsoport;

        public Athlete(String raceNumber, String name, String egyesulet, String korcsoport) {
            this.raceNumber = raceNumber;
            this.name = name;
            this.egyesulet = egyesulet;
            this.korcsoport = korcsoport;
        }

        public String getRaceNumber() {
            return raceNumber;
        }

        public String getName() {
            return name;
        }

        public String getEgyesulet() {
            return egyesulet;
        }

        public String getKorcsoport() {
            return korcsoport;
        }
    }
}
