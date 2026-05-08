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
        String[] alarmSeverities = {"LOW", "MEDIUM", "HIGH", "CRITICAL"};
        String[] maintTypes = {"PLANNED", "EMERGENCY", "INSPECTION"};

        for (int i = 0; i < 15; i++) {
            String sev = (i < 5) ? "CRITICAL" : alarmSeverities[i % 4];
            SensorReading[] readings = {new SensorReading("TEMPERATURE", 85.0 + i)};

            farm.addLog(new AlarmEntry(
                    LocalDateTime.of(2024, 1, (i % 25) + 1, (i * 1) % 24, (i * 4) % 60),
                    (i % 3 == 0) ? "T001" : "T002",
                    "ALARM",
                    ops[i % 3],
                    readings,
                    "ERR-" + (100 + i),
                    sev
            ));
        }


        for (int i = 0; i < 10; i++) {
            SensorReading[] readings = {new SensorReading("OIL_PRESSURE", 3.1)};

            farm.addLog(new MaintenanceEntry(
                    LocalDateTime.of(2024, 2, (i % 25) + 1, 7 + (i % 10), (i * 5) % 60),
                    (i % 2 == 0) ? "T002" : "T003",
                    "MAINTENANCE",
                    ops[i % 3],
                    readings,
                    maintTypes[i % 3],
                    2.0 + i
            ));
        }


        for (int i = 0; i < 25; i++) {
            int month = 3 + (i / 7);
            SensorReading[] readings = {
                    new SensorReading("POWER", 2500 + i * 50),
                    new SensorReading("WIND_SPEED", 11.0 + (i % 4))
            };

            String turbineId = (i % 3 == 0) ? "T001" : (i % 3 == 1 ? "T002" : "T003");

            farm.addLog(new OperationalEntry(
                    LocalDateTime.of(2024, month, (i % 25) + 1, (i * 3) % 24, (i * 2) % 60),
                    turbineId,
                    (i % 10 == 0) ? "STARTUP" : "OPERATIONAL",
                    ops[i % 3],
                    readings
            ));
        }

        return farm;
    }
}