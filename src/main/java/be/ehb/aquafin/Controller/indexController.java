package be.ehb.aquafin.Controller;

import be.ehb.aquafin.Service.NeerslagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Controller voor de hoofdpagina van de Aquafin neerslagapplicatie.
 * Verzorgt de weergave van neerslaginformatie, voorspellingen en overstromingswaarschuwingen.
 */
@Controller
public class indexController {

    @Autowired
    private NeerslagService neerslagService;

    /**
     * Verwerkt GET-verzoeken voor de hoofdpagina ("/").
     *
     * Verzamelt en bereidt de volgende data voor:
     * - Grafiekdata voor neerslagvisualisatie (5 dagen)
     * - Tekstuele neerslagvoorspelling
     * - Overstromingswaarschuwingen (over 3 dagen)
     *
     * @param model Spring MVC Model voor het doorgeven van data aan de view
     * @return String naam van de view template ("index")
     */
    @GetMapping("/")
    public String index(Model model) {
        // Haal grafiekdata op voor 5 dagen
        Map<String, Object> grafiekData = neerslagService.getGrafiekData(5);

        // Zet grafiekdata om naar JSON voor JavaScript gebruik
        ObjectMapper mapper = new ObjectMapper();
        try {
            String regenGrafiekData = mapper.writeValueAsString(grafiekData);
            model.addAttribute("regenGrafiekData", regenGrafiekData);
        } catch (Exception e) {
            // Bij fout, stuur leeg JSON object
            model.addAttribute("regenGrafiekData", "{}");
        }

        // Voeg afzonderlijke grafiekcomponenten toe aan model
        model.addAttribute("grafiekDatums", grafiekData.get("datums"));
        model.addAttribute("grafiekGemiddelde", grafiekData.get("gemiddelde"));
        model.addAttribute("grafiekPerStad", grafiekData.get("perStad"));

        // Voeg tekstuele voorspelling toe
        String regenVoorspelling = neerslagService.getRegenVoorspellingTekst();
        model.addAttribute("regenVoorspelling", regenVoorspelling);

        // Bereid overstromingswaarschuwingen voor (3 dagen vooruit)
        Map<String, Object> waarschuwing = neerslagService.getOverstromingsWaarschuwing(3);
        model.addAttribute("overstromingsgevaar", waarschuwing.get("gevaar"));
        model.addAttribute("risicosteden", waarschuwing.get("risicosteden"));
        model.addAttribute("risicoDrempel", String.format("%.1f", waarschuwing.get("drempel")));

        return "index";
    }
}
