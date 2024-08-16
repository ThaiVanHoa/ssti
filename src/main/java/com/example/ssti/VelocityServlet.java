package com.example.ssti;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;

@WebServlet("/velocity")
public class VelocityServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        super.init();
        // Initialize Velocity engine with properties
        Properties props = new Properties();
        props.setProperty("resource.loader", "class");
        props.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        props.setProperty("runtime.references.strict", "true");
        try {
            Velocity.init(props);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // Create a Velocity context and add data
        VelocityContext context = new VelocityContext();

        // Get user input
        String userInput = request.getParameter("input");

        // Convert Velocity syntax to JavaScript syntax
        String jsExpression = convertToJavaScript(userInput);

        // Evaluate the expression using GraalVM JavaScript engine
        String evaluatedInput = evaluateExpression(jsExpression);
        context.put("userInput", evaluatedInput);

        // Load the template
        Template template;
        try {
            template = getTemplate();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Merge the template with data
        StringWriter writer = new StringWriter();
        template.merge(context, writer);

        // Set response content type
        response.setContentType("text/html");

        // Write response
        response.getWriter().write(writer.toString());
    }

    private Template getTemplate() throws Exception {
        // Load the template from the classpath
        return Velocity.getTemplate("template.vm");
    }

    // Method to evaluate mathematical expressions using GraalVM
    private String evaluateExpression(String expression) {
        try (Context context = Context.create()) {
            Value result = context.eval("js", expression);
            return result.toString();
        } catch (Exception e) {
            return "Error evaluating expression: " + e.getMessage();
        }
    }

    // Method to convert Velocity syntax to JavaScript syntax
    private String convertToJavaScript(String input) {
        if (input != null && input.startsWith("${") && input.endsWith("}")) {
            // Remove ${ and }
            return input.substring(2, input.length() - 1);
        }
        return input;
    }
}
