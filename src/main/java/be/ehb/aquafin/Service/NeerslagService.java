package be.ehb.aquafin.Service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.*;

@Service
public class NeerslagService {

    private static final Map<String, double[]> STEDEN = Map.of(
            "Antwerpen", new double[]{51.22, 4.40},
            "Gent", new double[]{51.05, 3.72},
            "Brugge", new double[]{51.21, 3.22},
            "Leuven", new double[]{50.88, 4.70},
            "Hasselt", new double[]{50.93, 5.34},
            "Brussel", new double[]{50.85, 4.35},
            "Kortrijk", new double[]{50.83, 3.26},
            "Aalst", new double[]{50.94, 4.04},
            "Mechelen", new double[]{51.03, 4.48},
            "Sint-Niklaas", new double[]{51.17, 4.14}
    );

    // --- GRAFIEKDATA ---
    public Map<String, Object> getGrafiekData(int dagen) {
        Map<String, List<Double>> neerslagPerStad = new LinkedHashMap<>();
        List<String> datums = new ArrayList<>();
        int aantalSteden = STEDEN.size();

        RestTemplate restTemplate = new RestTemplate();

        for (var entry : STEDEN.entrySet()) {
            String stad = entry.getKey();
            double[] coords = entry.getValue();
            String url = String.format(
                    Locale.US,
                    "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&daily=precipitation_sum&forecast_days=%d&timezone=auto",
                    coords[0], coords[1], dagen
            );
            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(response);
            JSONArray neerslag = json.getJSONObject("daily").getJSONArray("precipitation_sum");
            JSONArray times = json.getJSONObject("daily").getJSONArray("time");

            if (datums.isEmpty()) {
                for (int i = 0; i < dagen; i++) {
                    datums.add(times.getString(i));
                }
            }

            List<Double> waarden = new ArrayList<>();
            for (int i = 0; i < dagen; i++) {
                waarden.add(neerslag.getDouble(i));
            }
            neerslagPerStad.put(stad, waarden);
        }

        //  gemiddelde per dag berekenen
        List<Double> gemiddelde = new ArrayList<>();
        for (int dag = 0; dag < dagen; dag++) {
            double som = 0.0;
            for (List<Double> waardes : neerslagPerStad.values()) {
                som += waardes.get(dag);
            }
            gemiddelde.add(som / aantalSteden);
        }

        Map<String, Object> grafiekData = new HashMap<>();
        grafiekData.put("datums", datums);
        grafiekData.put("gemiddelde", gemiddelde);     // juiste key en waarde!
        grafiekData.put("perStad", neerslagPerStad);   // juiste key en waarde!
        return grafiekData;
    }

    public String getRegenVoorspellingTekst() {
        int dagen = 5;
        Map<String, List<Double>> stadNaarNeerslag = new LinkedHashMap<>();
        List<String> datums = new ArrayList<>();
        RestTemplate restTemplate = new RestTemplate();

        for (var entry : STEDEN.entrySet()) {
            String stad = entry.getKey();
            double[] coords = entry.getValue();
            String url = String.format(
                    Locale.US,
                    "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&daily=precipitation_sum&forecast_days=%d&timezone=auto",
                    coords[0], coords[1], dagen
            );
            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(response);
            JSONArray neerslag = json.getJSONObject("daily").getJSONArray("precipitation_sum");
            JSONArray times = json.getJSONObject("daily").getJSONArray("time");

            if (datums.isEmpty()) {
                for (int i = 0; i < dagen; i++) {
                    datums.add(times.getString(i));
                }
            }

            List<Double> waarden = new ArrayList<>();
            for (int i = 0; i < dagen; i++) {
                waarden.add(neerslag.getDouble(i));
            }
            stadNaarNeerslag.put(stad, waarden);
        }

        StringBuilder resultaat = new StringBuilder("Gemiddelde voorspelde neerslag voor vlaanderen (in mm):\n");
        for (int dag = 0; dag < dagen; dag++) {
            double som = 0.0;
            for (List<Double> waarden : stadNaarNeerslag.values()) {
                som += waarden.get(dag);
            }
            double gemiddelde = som / stadNaarNeerslag.size();
            resultaat.append(String.format("%s: %.1f mm\n", datums.get(dag), gemiddelde));
        }

        resultaat.append("\nDetails per stad:\n");
        for (String stad : stadNaarNeerslag.keySet()) {
            resultaat.append(stad).append(": ");
            List<Double> waardes = stadNaarNeerslag.get(stad);
            for (int dag = 0; dag < dagen; dag++) {
                resultaat.append(String.format("%.1f", waardes.get(dag)));
                if (dag != dagen - 1) resultaat.append(", ");
            }
            resultaat.append(" mm\n");
        }
        return resultaat.toString();
    }

    // --- OVERSTROMINGSWAARSCHUWING ---
    public Map<String, Object> getOverstromingsWaarschuwing(int dagen) {
        Map<String, Object> resultaat = new HashMap<>();
        double drempel = 1.0;

        List<String> risicosteden = new ArrayList<>();
        Map<String, Double> stadWaarde = new LinkedHashMap<>();

        for (var entry : STEDEN.entrySet()) {
            String stad = entry.getKey();
            double[] coords = entry.getValue();
            String url = String.format(
                    Locale.US,
                    "https://api.open-meteo.com/v1/forecast?latitude=%.4f&longitude=%.4f&daily=precipitation_sum&forecast_days=%d&timezone=auto",
                    coords[0], coords[1], dagen
            );
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);
            JSONObject json = new JSONObject(response);
            JSONArray neerslag = json.getJSONObject("daily").getJSONArray("precipitation_sum");

            double som = 0.0;
            for (int i = 0; i < dagen; i++) {
                som += neerslag.getDouble(i);
            }
            stadWaarde.put(stad, som);
            if (som >= drempel) {
                risicosteden.add(stad + " (" + String.format("%.1f", som) + "mm)");
            }
        }
        boolean gevaar = !risicosteden.isEmpty();
        resultaat.put("gevaar", gevaar);
        resultaat.put("risicosteden", risicosteden);
        resultaat.put("drempel", drempel);

        return resultaat;
    }
}
