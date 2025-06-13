package be.ehb.aquafin.Controller;

import be.ehb.aquafin.Service.NeerslagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller voor het tonen van historische neerslagdata van 2005 tot heden.
 * Verwerkt de weergave van maandelijkse neerslaggegevens per jaar.
 */
@Controller
public class vorigeDataController {
    @Autowired
    private NeerslagService neerslagService;

    /**
     * Verwerkt GET-verzoeken voor de pagina met historische data ("/vorigeData").
     *
     * Bereidt de volgende data voor:
     * - Historische neerslagdata per maand van 2005 tot 2024
     * - Beschikbare data voor 2025 (tot mei)
     * - Nog niet beschikbare maanden worden aangeduid met "X"
     *
     * Data structuur:
     * - Jaartallen van 2005 tot 2025
     * - Per jaar 12 maanden met neerslagwaarden in mm
     * - Voor 2025: alleen waarden voor jan-mei, rest "X"
     *
     * @param model Spring MVC Model voor het doorgeven van data aan de view
     * @return String naam van de view template ("vorigeData")
     */
    @GetMapping("/vorigeData")
    public String vorigeData(Model model) {
        // Creëer een lijst voor alle neerslagdata
        List<Map<String, Object>> regenData = new ArrayList<>();

        // Matrix met historische neerslagdata (jaar, jan-dec waarden)
        int[][] data = {
            // [jaar, jan, feb, ..., dec]
            {2005, 67, 54, 73, 68, 74, 79, 98, 89, 68, 98, 98, 108},
            // ... overige jaren ...
            {2024, 61, 58, 66, 61, 75, 77, 101, 88, 60, 96, 97, 114}
        };

        // Maandnamen voor de kolomkoppen
        String[] maanden = {"jaar", "jan", "feb", "mrt", "apr", "mei", "jun",
                          "jul", "aug", "sep", "okt", "nov", "dec"};

        // Converteer matrix naar lijst van maps (jaar -> maandwaarden)
        for (int[] rij : data) {
            Map<String, Object> jaarData = new LinkedHashMap<>();
            for (int i = 0; i < maanden.length; i++) {
                jaarData.put(maanden[i], rij[i]);
            }
            regenData.add(jaarData);
        }

        // Voeg huidige jaar (2025) toe met beschikbare data
        Map<String, Object> jaar2025 = new LinkedHashMap<>();
        jaar2025.put("jaar", 2025);
        jaar2025.put("jan", 65);
        jaar2025.put("feb", 56);
        jaar2025.put("mrt", 68);
        jaar2025.put("apr", 62);
        jaar2025.put("mei", 80);
        // Markeer toekomstige maanden met "X"
        jaar2025.put("jun", "X");
        jaar2025.put("jul", "X");
        jaar2025.put("aug", "X");
        jaar2025.put("sep", "X");
        jaar2025.put("okt", "X");
        jaar2025.put("nov", "X");
        jaar2025.put("dec", "X");
        regenData.add(jaar2025);

        // Voeg data toe aan model voor weergave in view
        model.addAttribute("regenData", regenData);

        return "vorigeData";
    }
}
