package com.packtpub.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

@Controller
public class BaseController {

    @RequestMapping(value = "/{input}", method = RequestMethod.GET, produces = "application/json")
    @ResponseBody
    public String calculateResult(@PathVariable int input, ModelMap model) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            final int result = (int) (Math.random()*100);
            resultMap.put("result", result);
        } catch(Exception e) {
            String message = String.format("%s: %s", e.getClass().toString(), e.getMessage());
            resultMap.put("result", message);
        }

        return getJson(resultMap);
    }

    private String getJson(Map<String, Object> map) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            throw new RuntimeException();
        }
    }

}
