package core.mvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import core.mvc.util.JsonUtils;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

public class JsonView implements View {
    private ObjectMapper om;

    public JsonView(ObjectMapper om) {
        this.om = om;
    }

    @Override
    public void render(Map<String, ?> model, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

        if (model.isEmpty()) {
            return;
        }

        writeContents(toJson(model), response);
    }

    private void writeContents(String content, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        out.write(content);
        out.flush();
        out.close();
    }

    private String toJson(Map<String, ?> model) {
        Set<String> keys = model.keySet();

        if (keys.size() == 1) {
            String key = keys.stream()
                    .findFirst()
                    .orElseThrow(NullPointerException::new);

            return JsonUtils.toJson(om, model.get(key));
        }

        return JsonUtils.toJson(om, model);
    }
}
