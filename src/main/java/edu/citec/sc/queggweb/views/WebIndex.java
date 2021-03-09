package edu.citec.sc.queggweb.views;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebIndex {

    @GetMapping("/")
    public String index(@RequestParam(name="name", required=false, defaultValue="") String name, Model model) {
        model.addAttribute("name", name);
        return "index";
    }

}
