package zechs.zplex.ui.error

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import dagger.hilt.android.AndroidEntryPoint
import zechs.zplex.R
import zechs.zplex.databinding.ActivityErrorBinding
import zechs.zplex.ui.main.MainActivity
import zechs.zplex.utils.ext.ifNullOrEmpty
import zechs.zplex.utils.ext.isTelevision
import zechs.zplex.utils.ext.showTelevisionFocus

@AndroidEntryPoint
class ErrorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityErrorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityErrorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val errorMessage = intent.getStringExtra(EXTRA_ERROR_MESSAGE)
            ?: getString(R.string.unknown_error)
        val stackTrace = intent.getStringExtra(EXTRA_STACK_TRACE)
        binding.errorMessageTextView.text = errorMessage.ifEmpty {
            getString(R.string.unknown_error)
        }
        binding.stackTraceTextView.text = stackTrace?.ifNullOrEmpty {
            getString(R.string.no_stack_trace)
        }
        if (isTelevision) setupTelevisionActions()
    }

    private fun setupTelevisionActions() {
        binding.root.showTelevisionFocus()
        binding.tvErrorHome?.apply {
            setOnClickListener {
                startActivity(Intent(this@ErrorActivity, MainActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                finish()
            }
            requestFocus()
        }
        val trace = binding.tvErrorTrace ?: return
        binding.tvErrorDetails?.apply {
            setOnClickListener {
                trace.isVisible = !trace.isVisible
                setText(if (trace.isVisible) R.string.tv_error_hide_details else R.string.tv_error_details)
            }
        }
    }

    companion object {
        const val EXTRA_ERROR_MESSAGE = "extra_error_message"
        const val EXTRA_STACK_TRACE = "extra_stack_trace"
    }
}
