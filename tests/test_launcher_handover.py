"""Safety checks for the public TV Home installation scripts."""

from pathlib import Path
import unittest


ROOT = Path(__file__).resolve().parents[1]
APP_ID = "com.tvhome.launcher"
MAIN_COMPONENT = "com.tvhome.launcher/com.gathin.tvhome.MainActivity"


class InstallerSafetyTest(unittest.TestCase):
    def scripts(self):
        for name in ("build-and-install.sh", "build-and-install.ps1"):
            yield name, (ROOT / "launcher" / name).read_text(encoding="utf-8")

    def test_scripts_target_the_public_application_id(self):
        for name, text in self.scripts():
            with self.subTest(script=name):
                self.assertIn(APP_ID, text)
                self.assertIn(MAIN_COMPONENT, text)
                self.assertNotIn("com.gathin.tvhome/.MainActivity", text)

    def test_scripts_never_disable_a_system_launcher(self):
        for name, text in self.scripts():
            with self.subTest(script=name):
                self.assertNotIn("pm disable", text.lower())
                self.assertNotIn("disable-user", text.lower())
                self.assertNotIn("STOCK_LAUNCHER", text)

    def test_scripts_verify_before_installing(self):
        for name, text in self.scripts():
            with self.subTest(script=name):
                self.assertIn("testReleaseUnitTest", text)
                self.assertIn("lintRelease", text)
                self.assertIn("assembleRelease", text)


if __name__ == "__main__":
    unittest.main()
