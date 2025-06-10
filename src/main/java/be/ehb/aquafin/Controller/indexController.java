package be.ehb.aquafin.Controller;

import be.ehb.aquafin.Service.NeerslagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.*;

@Controller
public class indexController {

    @Autowired
    private NeerslagService neerslagService;

    @GetMapping("/")
    public String index(Model model) {


        // Live data voor grafiek én tekst
        int dagen = 5;
        Map<String, Object> grafiekData = neerslagService.getGrafiekData(dagen);
        model.addAttribute("grafiekDatums", grafiekData.get("datums"));
        model.addAttribute("grafiekGemiddelde", grafiekData.get("gemiddelde"));
        model.addAttribute("grafiekPerStad", grafiekData.get("perStad"));

        // groene box
        String regenVoorspelling = neerslagService.getRegenVoorspellingTekst();
        model.addAttribute("regenVoorspelling", regenVoorspelling);

        // Overstromingsgevaar
        Map<String, Object> waarschuwing = neerslagService.getOverstromingsWaarschuwing(3);
        model.addAttribute("overstromingsgevaar", waarschuwing.get("gevaar"));
        model.addAttribute("risicosteden", waarschuwing.get("risicosteden"));
        model.addAttribute("risicoDrempel", String.format("%.1f", waarschuwing.get("drempel")));

        return "index";
    }
}
