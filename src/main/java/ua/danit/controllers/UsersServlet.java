package ua.danit.controllers;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import ua.danit.dao.LikedDAO;
import ua.danit.dao.UsersDAO;
import ua.danit.model.Yamnyk_liked;
import ua.danit.model.Yamnyk_users;
import ua.danit.utils.GeneratorID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class UsersServlet extends HttpServlet {
    private final ArrayList<Yamnyk_users> users;
    private final LikedDAO likedDAO;
    private static int counter = 0;

    public UsersServlet(UsersDAO userDAO, LikedDAO likedDAO) {
        this.users = userDAO.getAll();
        this.likedDAO = likedDAO    ;
    }


    @Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
        String appDir = System.getProperty("user.dir");
        cfg.setDirectoryForTemplateLoading(new File(appDir + "/lib/html"));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);

        Map<String, String> map = new HashMap<>();
        map.put("name", users.get(counter).getName());
        map.put("id", users.get(counter).getId().toString() );
        map.put("imgURL", users.get(counter).getImgURL());

        Template tmpl = cfg.getTemplate("users.html");
        Writer out = resp.getWriter();
        try {
            tmpl.process(map, out);
            counter++;
        } catch (TemplateException e1) {
            e1.printStackTrace();
        }
    }
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String liked = req.getParameter("liked");

        if(liked!=null){
            /**
             * TODO: HERE IS THE FUCKING PROBLEM
             * this thing or not saving any like,
             * or save it in both blocks
             * as result in DB exist two same likes or not exist any
             *
             * ...its so sad...I've been writing this shit all day...
             * its almost 2 am...need to get up at 6 am...have no words to say...
             */
            for(Yamnyk_liked likedUSR : likedDAO.getLiked()){
                if(liked.equals(likedUSR.getWhom())){
                    Yamnyk_liked lkd = new Yamnyk_liked();
                    lkd.setLike_id(likedUSR.getLike_id());
                    lkd.setWho((long) 123);
                    lkd.setWhom(Long.valueOf(liked));
                    lkd.setTime(new Timestamp(System.currentTimeMillis()));
                    likedDAO.update(lkd);
                    break;
                } else if(likedDAO.getLiked().indexOf(likedUSR) == likedDAO.getLiked().size()
                        && !liked.equals(likedUSR.getWhom())){
                    Yamnyk_liked lkd = new Yamnyk_liked();
                    lkd.setWho((long) 123);
                    lkd.setWhom(Long.valueOf(liked));
                    lkd.setTime(new Timestamp(System.currentTimeMillis()));
                    likedDAO.save(lkd);
                }
            }
        }

        Configuration cfg = new Configuration(Configuration.VERSION_2_3_28);
        String appDir = System.getProperty("user.dir");
        cfg.setDirectoryForTemplateLoading(new File(appDir + "/lib/html"));
        cfg.setDefaultEncoding("UTF-8");
        cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        cfg.setLogTemplateExceptions(false);
        cfg.setWrapUncheckedExceptions(true);

        if(counter == users.size()){
            resp.sendRedirect("/liked");
            counter = 0;
        }

        Map<String, String> map = new HashMap<>();
        map.put("name", users.get(counter).getName());
        map.put("id", users.get(counter).getId().toString());
        map.put("imgURL", users.get(counter).getImgURL());

        Template tmpl = cfg.getTemplate("users.html");
        Writer out = resp.getWriter();
        try {
            tmpl.process(map, out);
            counter++;
        } catch (TemplateException e1) {
            e1.printStackTrace();
        }
	}
}
