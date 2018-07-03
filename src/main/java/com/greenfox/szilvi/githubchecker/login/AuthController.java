package com.greenfox.szilvi.githubchecker.login;

import com.greenfox.szilvi.githubchecker.user.service.UserHandling;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import static com.greenfox.szilvi.githubchecker.general.Settings.*;
import java.io.IOException;


@Controller
public class AuthController {

    @Value("${CLIENT_ID}")
    private String clientId;

    @Autowired
    Authorization authorization;

    @Autowired
    UserHandling userHandling;

    @RequestMapping("/login")
    public String renderLogin(){
        return "login";
    }

    @RequestMapping("/oauth")
    public String redirecttoOauth(){
        String url = Boolean.valueOf(IS_LOCALHOST) ? LOCALHOST : HEROKU;
        return "redirect:https://github.com/login/oauth/authorize?client_id=" + clientId + "&redirect_uri=" + url + "&scope=repo%20admin:org";
    }

    @RequestMapping("/auth")
    public String getAccessToken(@RequestParam String code, Model model) throws IOException {
        String accessToken = authorization.getAccessToken(code);
        System.out.println(accessToken);
        if (userHandling.checkIfUserMemberOfMentors(userHandling.getAuthUser())){
            userHandling.saveNewUser(accessToken);
            return "index";
        } else {
            model.addAttribute("notMentor", "Oooops, sorry, but only mentors can access this app!");
            System.setProperty(GITHUB_TOKEN, "");
            return "login";
        }
    }

    @RequestMapping("/logout")
    public String logout(){
        System.setProperty(GITHUB_TOKEN, "");
        return "login";
    }
}