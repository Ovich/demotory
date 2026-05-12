package com.blueur.demotory;

import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class RootController {
  private final HazelcastInstance hazelcastInstance;
  private final Counter mapChanges;

  public RootController(HazelcastInstance hazelcastInstance, MeterRegistry registry) {
    this.hazelcastInstance = hazelcastInstance;
    Gauge.builder("demotory.map.size", hazelcastInstance.getMap("entries"), Map::size)
         .description("Number of entries in the Hazelcast map")
         .register(registry);
    this.mapChanges = Counter.builder("demotory.map.changes")
         .description("Total number of map change events (put + delete)")
         .register(registry);
  }

  private IMap<String, String> entriesMap() {
    return hazelcastInstance.getMap("entries");
  }

  @GetMapping
  public String index(Model model) {
    final Map<String, String> entries = entriesMap().getAll(entriesMap().keySet());
    model.addAttribute("entries", entries);
    model.addAttribute("entry",
        new Entry(RandomStringUtils.randomAlphanumeric(2), RandomStringUtils.randomAlphanumeric(4)));
    return "index";
  }

  @PostMapping
  public String put(@ModelAttribute Entry entry) {
    entriesMap().put(entry.getKey(), entry.getValue());
    mapChanges.increment();
    log.info("Put {}={}", entry.getKey(), entry.getValue());
    return "redirect:.";
  }

  @DeleteMapping("/{key}")
  public String delete(@PathVariable String key) {
    entriesMap().delete(key);
    mapChanges.increment();
    log.info("Delete {}", key);
    return "redirect:.";
  }
}
