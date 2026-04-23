package data;

import models.*;
import java.time.LocalDateTime;

public class DaneStudenta {

    public static WindFarm utworzFarme() {
        WindFarm farm = new WindFarm("Farma Wiatrowa Północ", "Enea Operator", "Bałtyk");

        SensorReading[] s1 = {
                new SensorReading("POWER", 4000.5),
                new SensorReading("WIND_SPEED", 12.5),
                new SensorReading("TEMPERATURE", 25.0),
                new SensorReading("ROTOR_SPEED", 15.0),
                new SensorReading("OIL_PRESSURE", 4.2),
                new SensorReading("VIBRATION", 0.02)
        };

        SensorReading[] s2 = {
                new SensorReading("POWER", 2100.0),
                new SensorReading("WIND_SPEED", 8.2),
                new SensorReading("TEMPERATURE", 22.0),
                new SensorReading("ROTOR_SPEED", 11.0)
        };

        SensorReading[] s3 = {
                new SensorReading("POWER", 0.0),
                new SensorReading("WIND_SPEED", 1.5),
                new SensorReading("TEMPERATURE", 20.0)
        };

        WindTurbine t1 = new WindTurbine("T001", "Vestas V164", 9500, 110, s1);
        WindTurbine t2 = new WindTurbine("T002", "Siemens SWT", 4000, 90, s2);
        WindTurbine t3 = new WindTurbine("T003", "GE Haliade", 6000, 100, s3);

        farm.addTurbine(t1);
        farm.addTurbine(t2);
        farm.addTurbine(t3);

        String[] ops = {"Anna Kowalska", "Jan Nowak", "Piotr Zamek"};

        // === ALARM (10) ===
        for(int i = 0; i < 10; i++) {
            String turbineId;

            if (i % 3 == 0) {
                turbineId = "T001";
            } else if (i % 3 == 1) {
                turbineId = "T002";
            } else {
                turbineId = "T003";
            }

            SensorReading[] readings = {
                    new SensorReading("POWER", 100 + i * 10),
                    new SensorReading("WIND_SPEED", 2 + i),
                    new SensorReading("TEMPERATURE", 15 + i)
            };

            farm.addLog(new LogEntry(
                    LocalDateTime.of(2024, 1, 10 + i, 8 + (i % 10), 0),
                    turbineId,
                    "ALARM",
                    ops[i % 3],
                    readings
            ));
        }

        // === MAINTENANCE (5) ===
        for(int i = 0; i < 5; i++) {
            String turbineId;

            if (i % 2 == 0) {
                turbineId = "T002";
            } else {
                turbineId = "T001";
            }

            SensorReading[] readings = {
                    new SensorReading("POWER", 1500 + i * 100),
                    new SensorReading("WIND_SPEED", 6 + i),
                    new SensorReading("TEMPERATURE", 20 + i),
                    new SensorReading("ROTOR_SPEED", 10 + i)
            };

            farm.addLog(new LogEntry(
                    LocalDateTime.of(2024, 2, 10 + i, 10 + i, 0),
                    turbineId,
                    "MAINTENANCE",
                    ops[i % 3],
                    readings
            ));
        }

        String[] eventTypes = {"OPERATIONAL", "SHUTDOWN", "STARTUP", "ALARM", "MAINTENANCE"};
        // === OPERATIONAL T001 (15) ===
        for(int i = 0; i < 15; i++) {
            SensorReading[] readings = {
                    new SensorReading("POWER", 3000 + i * 50),
                    new SensorReading("WIND_SPEED", 10 + (i % 5)),
                    new SensorReading("TEMPERATURE", 18 + i),
                    new SensorReading("ROTOR_SPEED", 12 + i),
                    new SensorReading("OIL_PRESSURE", 4 + (i * 0.1)),
                    new SensorReading("VIBRATION", 0.01 + (i * 0.001))
            };

            String eventType = eventTypes[i % eventTypes.length];

            farm.addLog(new LogEntry(
                    LocalDateTime.of(2024, 3, i + 1, 12 + (i % 6), 0),
                    "T001",
                    eventType,
                    ops[i % 3],
                    readings
            ));
        }

        // === OPERATIONAL T002/T003 (20) ===
        for(int i = 0; i < 20; i++) {
            String turbineId = (i % 2 == 0) ? "T002" : "T003";

            SensorReading[] readings = {
                    new SensorReading("POWER", 2000 + i * 30),
                    new SensorReading("WIND_SPEED", 7 + (i % 4)),
                    new SensorReading("TEMPERATURE", 19 + (i % 10)),
                    new SensorReading("ROTOR_SPEED", 11 + i)
            };

            String eventType = eventTypes[i % eventTypes.length];

            farm.addLog(new LogEntry(
                    LocalDateTime.of(2024, 4, i + 1, 14 + (i % 5), 0),
                    turbineId,
                    eventType,
                    ops[i % 3],
                    readings
            ));
        }

        return farm;
    }
}