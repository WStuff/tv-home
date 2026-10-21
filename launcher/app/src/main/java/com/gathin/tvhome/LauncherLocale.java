package com.gathin.tvhome;

import android.content.Context;
import android.content.res.Configuration;
import android.os.LocaleList;

import java.util.Locale;

/** Keeps TV Home deliberately bilingual until more languages are designed and reviewed. */
final class LauncherLocale {
    private static final String PREFS = "launcher_language";
    private static final String KEY = "code";

    private LauncherLocale() { }

    static Context wrap(Context base) {
        Locale locale = new Locale(language(base));
        Locale.setDefault(locale);
        LocaleList.setDefault(new LocaleList(locale));
        Configuration configuration = new Configuration(base.getResources().getConfiguration());
        configuration.setLocale(locale);
        return base.createConfigurationContext(configuration);
    }

    static String language(Context context) {
        String saved = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY, null);
        if ("ru".equals(saved) || "en".equals(saved)) return saved;
        // The TV's resource configuration is authoritative. java.util.Locale may still be the
        // build machine/default process locale during an Activity's first attachment.
        Locale device = context.getResources().getConfiguration().getLocales().get(0);
        return "ru".equals(device.getLanguage()) ? "ru" : "en";
    }

    static void save(Context context, String language) {
        if (!"ru".equals(language)) language = "en";
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY, language).apply();
    }
}
