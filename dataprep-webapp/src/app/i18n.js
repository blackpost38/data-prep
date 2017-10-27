import i18n from 'i18next';
import XHR from 'i18next-xhr-backend';

i18n // eslint-disable-line import/no-named-as-default-member
	.use(XHR)
	.init({
		// locales load path. lng = language, ns = namespace
		backend: {
			loadPath: '/i18n/{{lng}}.json',
		},

		// Fallback language
		fallbackLng: 'en',

		debug: false,
		wait: true, // globally set to wait for loaded translations in translate hoc
	})
	.changeLanguage('fr');


export default i18n;
