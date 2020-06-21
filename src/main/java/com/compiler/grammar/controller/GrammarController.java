package com.compiler.grammar.controller;

import com.compiler.grammar.constant.GrammarConstant;
import com.compiler.grammar.constant.Msg;
import com.compiler.grammar.entity.*;
import com.compiler.grammar.exception.GrammarException;
import com.compiler.grammar.service.GrammarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;

/**
 * @author 10652
 */
@Controller
@RequestMapping("/grammar")
public class GrammarController {
    private final GrammarService service;

    @Autowired
    public GrammarController(GrammarService service) {
        this.service = service;
    }


    @GetMapping
    public String index(Model model) {
        HashMap<String, String> map = new HashMap<>(3);
        map.put("final", GrammarConstant.FINAL_SYMBOL);
        map.put("init", GrammarConstant.INIT_SYMBOL);
        map.put("none", GrammarConstant.NON);
        HashMap<String, Object> result = new HashMap<>(2);
        result.put("regularGrammar", new Result(1, Msg.SUCCESS, new RegularGrammar()));
        result.put("dfa", new Result(1, Msg.SUCCESS, new DfaVO()));
        result.put("expression", new Result(1, Msg.SUCCESS, ""));
        model.addAttribute("data", result);
        model.addAllAttributes(map);
        return "index.html";
    }

    @PostMapping(value = "/transform")
    public String transformGrammar(@RequestBody RegularGrammar regularGrammar, Model model) {
        Result result = service.transformGrammar(regularGrammar);
        if (result.getCode() == 0) {
            throw new GrammarException(result.getMsg());
        }
        model.addAttribute("data", result.getBody());
        return "transform.html";
    }

    @ResponseBody
    @PostMapping("/filter")
    public ModelAndView filterSensitiveWord(@RequestBody Sensitive sensitive, Model model) {
        Result result = service.filterSensitiveWord(sensitive);
        if (result.getCode() == 0) {
            throw new GrammarException(result.getMsg());
        }
        model.addAttribute("words", result.getBody());
        return new ModelAndView("application.html :: #sensitiveWords", "filter", result.getBody());
    }
}
