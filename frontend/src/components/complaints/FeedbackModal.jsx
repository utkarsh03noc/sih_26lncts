import React, { useState } from 'react';
import Modal from '../common/Modal';
import { Star } from 'lucide-react';

const FeedbackModal = ({ isOpen, onClose, complaintNumber, onSubmit }) => {
  const [rating, setRating] = useState(5);
  const [comments, setComments] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      setError('');
      await onSubmit({ rating, comments });
      onClose();
    } catch (err) {
      setError(err.response?.data?.message || err.message || 'Failed to submit feedback');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Resolution Feedback & Satisfaction">
      <form onSubmit={handleSubmit} className="space-y-4 text-xs">
        {error && (
          <div className="p-3 rounded-lg bg-rose-50 border border-rose-200 text-rose-700 font-medium">
            {error}
          </div>
        )}

        <p className="text-slate-600">
          How satisfied are you with the redressal of complaint{' '}
          <strong className="text-slate-900 font-mono">{complaintNumber}</strong>?
        </p>

        {/* 5-Star Rating */}
        <div>
          <label className="block font-semibold text-slate-700 mb-2">
            Your Rating <span className="text-rose-500">*</span>
          </label>
          <div className="flex items-center space-x-2">
            {[1, 2, 3, 4, 5].map((star) => (
              <button
                key={star}
                type="button"
                onClick={() => setRating(star)}
                className="p-1 focus:outline-none transition-transform hover:scale-110"
              >
                <Star
                  className={`w-7 h-7 ${
                    star <= rating ? 'text-amber-400 fill-amber-400' : 'text-slate-300'
                  }`}
                />
              </button>
            ))}
            <span className="ml-2 font-semibold text-slate-700">
              {rating === 5
                ? 'Excellent'
                : rating === 4
                ? 'Good'
                : rating === 3
                ? 'Satisfactory'
                : rating === 2
                ? 'Poor'
                : 'Very Unsatisfied'}
            </span>
          </div>
        </div>

        <div>
          <label className="block font-semibold text-slate-700 mb-1">
            Comments & Observations (Optional)
          </label>
          <textarea
            rows={3}
            value={comments}
            onChange={(e) => setComments(e.target.value)}
            placeholder="Share feedback on resolution speed, communication quality, or fairness..."
            className="w-full px-3 py-2 text-xs border border-slate-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-brand-500"
          />
        </div>

        <div className="flex justify-end space-x-2 pt-2 border-t border-slate-200">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 font-medium text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg transition-colors"
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={submitting}
            className="px-4 py-2 font-semibold text-white bg-brand-600 hover:bg-brand-700 rounded-lg transition-colors disabled:opacity-50"
          >
            {submitting ? 'Submitting...' : 'Submit Feedback'}
          </button>
        </div>
      </form>
    </Modal>
  );
};

export default FeedbackModal;
