package com.joker.community.controller;

import com.joker.community.dto.AccessTokenDTO;
import com.joker.community.mapper.UserMapper;
import com.joker.community.model.User;
import com.joker.community.provider.GithubProvider;
import com.joker.community.provider.GithubUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

@Controller
public class AuthorizeController {

    @Autowired
    GithubProvider provider;

    @Value("@{github.client.id}")
    private String clientId;

    @Value("@{github.client.secret}")
    private String clientSecret;

    @Value("@{github.redirect.uri}")
    private String redirectUri;

    @Autowired
    private UserMapper userMapper;

    @GetMapping("callback")
    public String callback(@RequestParam(name = "code") String code,
                           @RequestParam(name = "state") String state,
                           HttpServletRequest request) {
        AccessTokenDTO accessTokenDTO = new AccessTokenDTO();
        accessTokenDTO.setCode(code);
        accessTokenDTO.setState(state);
        accessTokenDTO.setClient_id(clientId);
        accessTokenDTO.setClient_secret(clientSecret);
        accessTokenDTO.setRedirect_uri(redirectUri);
        String accessToken = provider.getAccessToken(accessTokenDTO);
        GithubUser githubUser = provider.getUser(accessToken);

        if (githubUser != null) {
            User user = new User();
            user.setToken(String.valueOf(UUID.randomUUID()));
            user.setName(githubUser.getName());
            user.setAccountId(String.valueOf(githubUser.getId()));
            user.setGmtModified(user.getGmtCreate());
            user.setGmtCreate(System.currentTimeMillis());
            userMapper.insert(user);
            request.getSession().setAttribute("user", user);
            return "redirect:/";
        } else {
            return "redirect:/";
        }
    }

}
