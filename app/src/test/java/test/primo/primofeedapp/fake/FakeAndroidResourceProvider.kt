package test.primo.primofeedapp.fake

import test.primo.primofeedapp.core.provider.AndroidResourceProvider

class FakeAndroidResourceProvider : AndroidResourceProvider {
        private var template: String? = null
        private var shouldThrowException = false

        fun setTemplate(template: String?) {
            this.template = template
        }

        fun setShouldThrowException(shouldThrow: Boolean) {
            this.shouldThrowException = shouldThrow
        }

        override fun getStringFromAssert(fileName: String): String? {
            if (shouldThrowException) {
                throw RuntimeException("Fake exception for testing")
            }
            return template
        }
    }