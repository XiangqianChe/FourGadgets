package algonquin.cst2335.fourgadgets.entity;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/** Entity class for flight record
 * @author Victor Che
 * @version 1.0
 */
@Entity
public class FlightRecord {
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public Long id;
    @ColumnInfo(name = "airportCode")
    public String airportCode;
    @ColumnInfo(name = "destination")
    public String destination;
    @ColumnInfo(name = "flightDate")
    public String flightDate;
    @ColumnInfo(name = "flightNumber")
    public String flightNumber;
    @ColumnInfo(name = "terminal")
    public String terminal;
    @ColumnInfo(name = "gate")
    public String gate;
    @ColumnInfo(name = "delay")
    public String delay;
    public FlightRecord(String airportCode, String destination, String flightDate, String flightNumber, String terminal, String gate, String delay) {
        this.airportCode = airportCode;
        this.destination = destination;
        this.flightDate = flightDate;
        this.flightNumber = flightNumber;
        this.terminal = terminal;
        this.gate = gate;
        this.delay = delay;
    }
    public Long getId() {
        return id;
    }
    public String getAirportCode() {
        return airportCode;
    }
    public String getDestination() {
        return destination;
    }
    public String getFlightDate() {
        return flightDate;
    }
    public String getFlightNumber() {
        return flightNumber;
    }
    public String getTerminal() {
        return terminal;
    }
    public String getGate() {
        return gate;
    }
    public String getDelay() {
        return delay;
    }
}
